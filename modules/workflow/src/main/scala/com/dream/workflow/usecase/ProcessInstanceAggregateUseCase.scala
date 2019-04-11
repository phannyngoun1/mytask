package com.dream.workflow.usecase

import java.time.Instant
import java.util.UUID

import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl.{Flow, _}
import com.dream.common.Protocol.TaskPerformCmdRequest
import com.dream.common._
import com.dream.common.domain.ResponseError
import com.dream.workflow.domain.{Flow => WFlow, _}
import com.dream.workflow.entity.processinstance.ProcessInstanceProtocol.{CreatePInstCmdRequest => CreateInst}
import com.dream.workflow.usecase.ItemAggregateUseCase.Protocol.{GetItemCmdRequest, GetItemCmdSuccess}
import com.dream.workflow.usecase.ParticipantAggregateUseCase.Protocol.{AssignTaskCmdReq, AssignTaskCmdRes, AssignTaskCmdSuccess}
import com.dream.workflow.usecase.WorkflowAggregateUseCase.Protocol.{GetWorkflowCmdRequest, GetWorkflowCmdSuccess}
import com.dream.workflow.usecase.port._

import scala.concurrent.{ExecutionContext, Future, Promise}


object ProcessInstanceAggregateUseCase {

  object Protocol {

    sealed trait ProcessInstanceCmdResponse

    sealed trait ProcessInstanceCmdRequest

    sealed trait CreateInstanceCmdResponse extends ProcessInstanceCmdResponse

    case class CreatePInstCmdRequest(
      itemID: UUID,
      by: UUID,
      params: Option[Params] = None
    ) extends ProcessInstanceCmdRequest

    sealed trait CreatePInstCmdResponse extends ProcessInstanceCmdResponse

    case class CreatePInstCmdSuccess(
      folio: String
    ) extends CreatePInstCmdResponse

    case class CreatePInstCmdFailed(error: ResponseError) extends CreatePInstCmdResponse

    case class GetPInstCmdRequest(id: UUID) extends ProcessInstanceCmdRequest

    sealed trait GetPInstCmdResponse extends ProcessInstanceCmdResponse

    case class GetPInstCmdSuccess(id: UUID, flowId: UUID, folio: String, tasks: List[Task]) extends GetPInstCmdResponse

    case class GetPInstCmdFailed(error: ResponseError) extends GetPInstCmdResponse

    case class GetTaskCmdReq(participantId: UUID, assignedTask: AssignedTask) extends ProcessInstanceCmdRequest

    sealed trait GetTaskCmdRes extends ProcessInstanceCmdResponse

    case class GetTaskCmdSuccess(taskDto: TaskDto) extends GetTaskCmdRes

    case class GetTaskCmdFailed(error: ResponseError) extends GetTaskCmdRes

    case class TakeActionCmdRequest(pInstId: UUID, taskId: UUID, action: BaseAction, participantId: UUID, payLoad: PayLoad) extends ProcessInstanceCmdRequest

    trait TakeActionCmdResponse extends ProcessInstanceCmdResponse

    case class TakeActionCmdSuccess() extends TakeActionCmdResponse

    case class TakeActionCmdFailed(error: ResponseError) extends TakeActionCmdResponse


    case class CreateNewTaskCmdRequest(
      id: UUID,
      task: Task,
      participantId: UUID
    ) extends ProcessInstanceCmdRequest

    trait CreateNewTaskCmdResponse extends ProcessInstanceCmdResponse

    case class CreateNewTaskCmdSuccess(
      id: UUID,
      taskId: UUID,
      destinations: List[UUID]
    ) extends CreateNewTaskCmdResponse

    case class CreateNewTaskCmdFailed(error: ResponseError) extends CreateNewTaskCmdResponse


    case class PerformTaskCmdReq(
      pInstId: UUID,
      taskId: UUID,
      action: BaseAction,
      activity: BaseActivity,
      payLoad: PayLoad,
      processBy: UUID
    ) extends TaskPerformCmdRequest

    sealed trait PerformTaskCmdRes extends ProcessInstanceCmdResponse

    case class PerformTaskSuccess(activityId: UUID) extends PerformTaskCmdRes

    case class PerformTaskFailed(error: ResponseError) extends PerformTaskCmdRes

    case class CommitActionCmdReq(id: UUID, taskId: UUID, participantId: UUID, action: BaseAction, processAt: Instant) extends ProcessInstanceCmdRequest

    sealed trait CommitActionCmdRes extends ProcessInstanceCmdResponse

    case class CommitActionCmdSuccess(id: UUID) extends CommitActionCmdRes

    case class CommitActionCmdFailed(error: ResponseError) extends CommitActionCmdRes

    case class ActionCompleted()

  }

}

class ProcessInstanceAggregateUseCase(

  processInstanceAggregateFlows: ProcessInstanceAggregateFlows,
  workflowAggregateFlows: WorkflowAggregateFlows,
  itemAggregateFlows: ItemAggregateFlows,
  participantAggregateFlows: ParticipantAggregateFlows,
  pInstanceReadModelFlows: PInstanceReadModelFlows

)(implicit system: ActorSystem)
  extends UseCaseSupport {

  import ProcessInstanceAggregateUseCase.Protocol._
  import UseCaseSupport._

  val decider: Supervision.Decider = {
    case _ => Supervision.Restart
  }

  implicit val mat = ActorMaterializer(
    ActorMaterializerSettings(system)
      .withSupervisionStrategy(decider)
  )

  private val getWorflowByItem =
    Flow[UUID].map(GetItemCmdRequest)
      .via(itemAggregateFlows.getItem)
      .map {
        case res: GetItemCmdSuccess => GetWorkflowCmdRequest(res.workflowId)
      }
      .via(workflowAggregateFlows.getWorkflow)
      .map {
        case GetWorkflowCmdSuccess(workflow) => workflow
      }


  //TODO: workaround, need to be fixed
  private val prepareCreateInst = Flow.fromGraph(GraphDSL.create() { implicit b =>
    import GraphDSL.Implicits._

    val broadcast = b.add(Broadcast[CreatePInstCmdRequest](2))
    val createInstZip = b.add(Zip[WFlow, CreatePInstCmdRequest])
    val convertToGetItem = Flow[CreatePInstCmdRequest].map(r => GetItemCmdRequest(r.itemID))
    val convertToCreatePInstCmdReq = Flow[(WFlow, CreatePInstCmdRequest)].map(
      f => {
        val flow = f._1
        val req = f._2
        val startAction = StartAction()
        val startActivity = StartActivity()
        val nextFlow = flow.nextActivity(startAction, startActivity, ParticipantAccess(req.by), false) match {
          case Right(flow) => flow
        }

        CreateInst(
          id = UUID.randomUUID(),
          createdBy = req.by,
          flowId = flow.id,
          folio = "test",
          contentType = "ticket",
          description = "Test",
          destIds = nextFlow.participants,
          task = Task(
            id = UUID.randomUUID(),
            activity = nextFlow.activity,
            actions = nextFlow.actionFlows.map(_.action),
            nextFlow.participants.map(TaskDestination(_))
          ),
          DefaultPayLoad("New ticket")
        )
      }
    )

    //    broadcast.out(0) ~> Flow[CreatePInstCmdRequest].map(_.itemID) ~> getWorflowByItem ~> createInstZip.in0

    broadcast.out(0) ~> convertToGetItem ~> itemAggregateFlows.getItem.map {
      case res: GetItemCmdSuccess => GetWorkflowCmdRequest(res.workflowId)
    } ~> workflowAggregateFlows.getWorkflow.map {
      case GetWorkflowCmdSuccess(workflow) => workflow
    } ~> createInstZip.in0

    broadcast.out(1) ~> createInstZip.in1

    val createPrepareB = b.add(Broadcast[CreateInst](3))
    val convertToTaskCmdRequestFlow = Flow[CreateInst].map(p => PerformTaskCmdReq(p.id, p.task.id, StartAction(), p.task.activity, p.payLoad, p.createdBy))

    //TODO: adding real tasks

    val assignTaskCmdFlow = Flow[CreateInst].flatMapConcat(p => Source(p.destIds.map(dest => AssignTaskCmdReq(dest, p.task.id, p.id))))

    val out = createInstZip.out ~> convertToCreatePInstCmdReq ~> createPrepareB ~> processInstanceAggregateFlows.createInst
    createPrepareB ~> convertToTaskCmdRequestFlow ~> processInstanceAggregateFlows.performTask ~> Sink.ignore
    createPrepareB ~> assignTaskCmdFlow ~> participantAggregateFlows.assignTask ~> Sink.ignore

    FlowShape(broadcast.in, out.outlet)
  })


  private case class TakeActionParams(
    flow: WFlow,
    task: TaskDto,
    action: TakeActionCmdRequest,
    actionDate: Instant = Instant.now(),
    nexActivity: Option[BaseActivityFlow] = None,
    newTaskId: Option[UUID] = None
  )

  private val takeActionFlowGraph = Flow.fromGraph(GraphDSL.create() { implicit b =>

    import GraphDSL.Implicits._
    val broadcast = b.add(Broadcast[TakeActionCmdRequest](3))
    val zipFlowTask = b.add(Zip[WFlow, TaskDto])
    val zipActionParam = b.add(Zip[(WFlow, TaskDto), TakeActionCmdRequest])

    val takeActonBroadcast = b.add(Broadcast[TakeActionParams](3))

    val mapToGetTaskReq = Flow[TakeActionCmdRequest].map(item => {
      println(" mapToGetTaskReq ")
      GetTaskCmdReq(item.participantId, AssignedTask(item.taskId, item.pInstId))
    })

    broadcast.out(0) ~> Flow[TakeActionCmdRequest].map(it => GetPInstCmdRequest(it.pInstId)) ~> processInstanceAggregateFlows.getPInst.map {
      case res: GetPInstCmdSuccess => {
        println(s"get PInsta  ${res.flowId} ")
        GetWorkflowCmdRequest(res.flowId)
      }
    } ~> workflowAggregateFlows.getWorkflow.map {
      case GetWorkflowCmdSuccess(workflow) => {
        println(s"workflow ${workflow}")
        workflow
      }
    } ~> zipFlowTask.in0

    broadcast.out(1) ~> mapToGetTaskReq ~> processInstanceAggregateFlows.getTask.map {
      case GetTaskCmdSuccess(dto) => {
        println(s"get tasks ${dto}")
        dto
      }
      case _ => {
        println(s"get tasks Error")
        throw new RuntimeException("Error -----------")
      }

    } ~> zipFlowTask.in1


    zipFlowTask.out ~> zipActionParam.in0
    broadcast.out(2) ~> zipActionParam.in1

    zipActionParam.out.map(f => TakeActionParams(f._1._1, f._1._2, f._2)) ~> Flow[TakeActionParams].map(
      f => f.flow.nextActivity(f.action.action, f.task.activity, ParticipantAccess(f.action.participantId)) match {
        case Right(activity) => f.copy(nexActivity = Some(activity), newTaskId = Some(UUID.randomUUID()))
      }
    ) ~> takeActonBroadcast.in


    val performTaskZip = b.add(Zip[PerformTaskCmdRes, CommitActionCmdRes])

    //perform task

    takeActonBroadcast.out(0) ~> Flow[TakeActionParams].map(
      f => PerformTaskCmdReq(f.action.pInstId, f.action.taskId, f.action.action, f.task.activity, f.action.payLoad, f.action.participantId)
    ) ~> processInstanceAggregateFlows.performTask ~> performTaskZip.in0
    
    takeActonBroadcast.out(1) ~> Flow[TakeActionParams].map(f =>
      CommitActionCmdReq(f.action.pInstId, f.action.taskId, f.action.participantId, f.action.action, f.actionDate)
    ) ~> processInstanceAggregateFlows.commitAction ~> performTaskZip.in1

    val out = performTaskZip.out ~> Flow[(PerformTaskCmdRes, CommitActionCmdRes)].map {
      case (a: PerformTaskSuccess, b: CommitActionCmdSuccess) =>
        ActionCompleted()
    }

    takeActonBroadcast.out(2) ~> Flow[TakeActionParams].map(item => {
      val param = item
      val newActivity = param.nexActivity.get
      CreateNewTaskCmdRequest(
        param.action.pInstId,
        Task(param.newTaskId.get, newActivity.activity, newActivity.actionFlows.map(_.action), newActivity.participants.map(TaskDestination(_))),
        param.action.participantId
      )
    }) ~> processInstanceAggregateFlows.createNewTask.map {
      case CreateNewTaskCmdSuccess(pInstId, taskId, dests) => dests.map(AssignTaskCmdReq(_, taskId, pInstId))
    }.flatMapConcat(Source(_)) ~> participantAggregateFlows.assignTask ~> Sink.ignore

    FlowShape(broadcast.in, out.outlet)

  })

  private val createInstanceFlow: SourceQueueWithComplete[(CreatePInstCmdRequest, Promise[CreatePInstCmdResponse])] = Source
    .queue[(CreatePInstCmdRequest, Promise[CreatePInstCmdResponse])](10, OverflowStrategy.dropNew)
    .via(prepareCreateInst.zipPromise)
    .toMat(completePromiseSink)(Keep.left)
    .run()

  private val takeActionFlow: SourceQueueWithComplete[(TakeActionCmdRequest, Promise[ActionCompleted])] = Source
    .queue[(TakeActionCmdRequest, Promise[ActionCompleted])](10, OverflowStrategy.dropNew)
    .via(takeActionFlowGraph.zipPromise)
    .toMat(completePromiseSink)(Keep.left)
    .run()

  private val getPInstFlow: SourceQueueWithComplete[(GetPInstCmdRequest, Promise[GetPInstCmdResponse])] = Source
    .queue[(GetPInstCmdRequest, Promise[GetPInstCmdResponse])](10, OverflowStrategy.dropNew)
    .via(processInstanceAggregateFlows.getPInst.zipPromise)
    .toMat(completePromiseSink)(Keep.left)
    .run()

  def takeAction(req: TakeActionCmdRequest)(implicit ec: ExecutionContext): Future[ActionCompleted] = {
    offerToQueue(takeActionFlow)(req, Promise())
  }

  def createPInst(request: CreatePInstCmdRequest)(implicit ec: ExecutionContext): Future[CreatePInstCmdResponse] = {
    offerToQueue(createInstanceFlow)(request, Promise())
  }

  def getPInst(request: GetPInstCmdRequest)(implicit ec: ExecutionContext): Future[GetPInstCmdResponse] =
    offerToQueue(getPInstFlow)(request, Promise())

  def list: Future[List[ProcessInstanceDto]] = {
    val sumSink = Sink.fold[List[ProcessInstanceDto], ProcessInstanceDto](List.empty[ProcessInstanceDto])((m, e) => e :: m)
    Source.fromPublisher(pInstanceReadModelFlows.list).toMat(sumSink)(Keep.right).run()
  }

}
