package com.dream.workflow.usecase

import java.time.Instant
import java.util.UUID

import akka.actor.ActorSystem
import akka.japi
import akka.stream._
import akka.stream.scaladsl.{Flow, _}
import com.dream.common.Protocol.TaskPerformCmdRequest
import com.dream.common._
import com.dream.common.domain.ResponseError
import com.dream.workflow.domain.{BaseActivityFlow, Flow => WFlow, _}
import com.dream.workflow.entity.processinstance.ProcessInstanceProtocol.{CreatePInstCmdRequest => CreateInst}
import com.dream.workflow.usecase.ItemAggregateUseCase.Protocol.{GetItemCmdRequest, GetItemCmdSuccess}
import com.dream.workflow.usecase.ParticipantAggregateUseCase.Protocol.{AssignTaskCmdReq, AssignTaskCmdSuccess}
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

    case class GetPInstCmdSuccess(id: UUID, flowId: UUID, folio: String, tasks: List[Task], active: Boolean) extends GetPInstCmdResponse

    case class GetPInstCmdFailed(error: ResponseError) extends GetPInstCmdResponse

    case class GetTaskCmdReq(participantId: UUID, assignedTask: AssignedTask) extends ProcessInstanceCmdRequest

    sealed trait GetTaskCmdRes extends ProcessInstanceCmdResponse

    case class GetTaskCmdSuccess(taskDto: TaskDto) extends GetTaskCmdRes

    case class GetTaskCmdFailed(error: ResponseError) extends GetTaskCmdRes

    case class TakeActionCmdRequest(
      pInstId: UUID, taskId: UUID, action: String, participantId: UUID, payLoad: Payload, comment: Option[String]
    ) extends ProcessInstanceCmdRequest

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
      actionPerformed: UUID,
      pInstId: UUID,
      taskId: UUID,
      action: BaseAction,
      activity: BaseActivity,
      payLoad: Payload,
      processBy: UUID
    ) extends TaskPerformCmdRequest

    sealed trait PerformTaskCmdRes extends ProcessInstanceCmdResponse

    case class PerformTaskSuccess(activityId: UUID) extends PerformTaskCmdRes

    case class PerformTaskFailed(error: ResponseError) extends PerformTaskCmdRes

    case class CommitActionCmdReq(
      id: UUID, actionPerformedId: UUID, taskId: UUID, participantId: UUID, action: BaseAction, processAt: Instant, comment: Option[String]
    ) extends ProcessInstanceCmdRequest

    sealed trait CommitActionCmdRes extends ProcessInstanceCmdResponse

    case class CommitActionCmdSuccess(id: UUID) extends CommitActionCmdRes

    case class CommitActionCmdFailed(error: ResponseError) extends CommitActionCmdRes



    case class ReRouteCmdReq(id: UUID, taskId: UUID,newParticipantId: UUID) extends ProcessInstanceCmdRequest
    sealed trait ReRouteCmdRes extends ProcessInstanceCmdResponse

    case class ReRouteCmdSuccess(id: UUID, taskId: UUID, participantId: UUID) extends ReRouteCmdRes
    case class ReRouteCmdFailed(error: ResponseError) extends ReRouteCmdRes

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

//  private val getWorflowByItem =
//    Flow[UUID].map(GetItemCmdRequest)
//      .via(itemAggregateFlows.getItem)
//      .map {
//        case res: GetItemCmdSuccess => GetWorkflowCmdRequest(res.workflowId)
//      }
//      .via(workflowAggregateFlows.getWorkflow)
//      .map {
//        case GetWorkflowCmdSuccess(workflow) => workflow
//      }


  //TODO: workaround, need to be fixed
  private val prepareCreateInst = Flow.fromGraph(GraphDSL.create() { implicit b =>
    import GraphDSL.Implicits._

    val broadcast = b.add(Broadcast[CreatePInstCmdRequest](2))
    val createInstZip = b.add(Zip[WFlow, CreatePInstCmdRequest])
    val convertToGetItem = Flow[CreatePInstCmdRequest].map(r => GetItemCmdRequest(r.itemID))
    val convertToCreatePInstCmdReq = Flow[(WFlow, CreatePInstCmdRequest)].map(
      f => {

        println(s"create process instance ${f}")

        val flow = f._1
        val req = f._2
        val startAction = StartAction()
        val startActivity = StartActivity()
        val nextFlow = flow.nextActivity(startAction, startActivity, ParticipantAccess(req.by), false) match {
          case Right(flow) => flow
          case _ => throw new RuntimeException("Flow not found")
        }
        val pInstId = UUID.randomUUID()
        val reqCmd = CreateInst(
          id =pInstId,
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
            nextFlow.participants.map(TaskDestination(_)),
            actionPerformed = List(ActionPerformed(pInstId, req.by, startAction, Instant.now(), None))
          ),
          DefaultPayLoad("New ticket")
        )

        println(req)
        reqCmd
      }
    )

    broadcast.out(0) ~> convertToGetItem ~> itemAggregateFlows.getItem.map {
      case res: GetItemCmdSuccess => {
        println(s"Receive item fetched ${res}")
        GetWorkflowCmdRequest(res.workflowId)
      }
      case _ => throw new RuntimeException(s"Failed to fetch item")
    } ~> workflowAggregateFlows.getWorkflow.map {
      case GetWorkflowCmdSuccess(workflow) => workflow
      case _ => throw new RuntimeException(s"Failed to fetch workflow")
    } ~> createInstZip.in0

    broadcast.out(1) ~> createInstZip.in1

    val createPrepareB = b.add(Broadcast[CreateInst](3))
    val convertToTaskCmdRequestFlow = Flow[CreateInst].map(p =>
      //TODO: consider more how to handler action performed ID.
      PerformTaskCmdReq(p.id , p.id, p.task.id, StartAction(), p.task.activity, p.payLoad, p.createdBy)
    )

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
    newTaskId: Option[UUID] = None,
    newDestination: List[UUID] = List.empty
  )

  private case class ActionParams(
    action: TakeActionCmdRequest,
    curAction: Option[BaseAction] = None,
    flow: Option[WFlow] = None,
    task: Option[TaskDto] = None,
    actionDate: Instant = Instant.now(),
    actionPerformedId: UUID = UUID.randomUUID(),
    nexActivity: Option[BaseActivityFlow] = None,
    newTaskId: Option[UUID] = None,
    newDestination: Option[UUID] = None
  )

  private val actionParamsFlowGraph = Flow.fromGraph(GraphDSL.create() { implicit b =>

    import GraphDSL.Implicits._
    val bCast = b.add(Broadcast[TakeActionCmdRequest](3))
    val zipFlow = b.add(Zip[ActionParams, WFlow])
    val zipTask = b.add(Zip[ActionParams, TaskDto])

    val mapToActionParam = Flow[TakeActionCmdRequest].map {req =>
      val newDest = req.payLoad match {
        case payload: ReRoutePayload => Some(payload.participantId)
        case _ => None
      }

      ActionParams(req, newDestination = newDest )
    }

    val mapToGetPInstCmdRequest = Flow[TakeActionCmdRequest].map(req => GetPInstCmdRequest(req.pInstId))
    val pInstance = processInstanceAggregateFlows.getPInst.map {

      case res: GetPInstCmdSuccess => {
        println(s"get PInstance  ${res.flowId} ")
        GetWorkflowCmdRequest(res.flowId)
      }
    }

    val fetchWorkflow = workflowAggregateFlows.getWorkflow.map {
      case GetWorkflowCmdSuccess(workflow) => {
        println(s"workflow ${workflow}")
        workflow
      }
    }

    // val workflowResult = Flow[WFlow].map(flow => ActionParams(flow = flow) )

    val mapZipFlowToActionParam = Flow[(ActionParams, WFlow)].map(f =>
      f._1.copy(
        flow = Some(f._2),
      )
    )

    val mapToGetTaskReq = Flow[TakeActionCmdRequest].map(item => {
      println(s" mapToGetTaskReq  ---- ${item}")
      GetTaskCmdReq(item.participantId, AssignedTask(item.taskId, item.pInstId))
    })

    val fetchTask = processInstanceAggregateFlows.getTask.map {
      case GetTaskCmdSuccess(dto) => {
        println(s"get tasks ${dto}")
        dto
      }
      case _ => {
        println(s"get tasks Error")
        throw new RuntimeException("Error -----------")
      }
    }

    val taskResult = Flow[(ActionParams, TaskDto)].map{ f =>

      println(s"Result ---------------------${f}----------- ")

      val action = f._1.flow.get.findCurrentActivity(f._2.activity) match {
        case Right(flow) => flow.actionFlows.find(_.action.name == f._1.action.action).map(_.action).getOrElse(NaAction())
        case _ => NaAction()
      }

      println(s"findCurrentActivity -------- ${action}------------------------ ")

      f._1.copy(
        task = Some(f._2),
        curAction = Some(action)
      )
    }

    bCast.out(0) ~> mapToActionParam ~> zipFlow.in0

    bCast.out(1) ~> mapToGetPInstCmdRequest ~> pInstance ~> fetchWorkflow ~> zipFlow.in1

    zipFlow.out ~> mapZipFlowToActionParam ~> zipTask.in0

    bCast.out(2) ~> mapToGetTaskReq ~> fetchTask ~> zipTask.in1

    val out = zipTask.out ~>  taskResult

    FlowShape(bCast.in, out.outlet)

  })


  private val takeActionFlowGraph = Flow.fromGraph(GraphDSL.create() { implicit b =>

    import GraphDSL.Implicits._
    val bCast = b.add(Broadcast[ActionParams](3))

    val taskCast = b.add(Broadcast[ActionParams](2))

    val performTaskZip = b.add(ZipWith[ActionCompleted, ActionCompleted, ActionCompleted]((a, _) => a))

    val mapToPerformTaskCmdReq = Flow[ActionParams].map { f =>

      println(" mapToPerformTaskCmdReq  ----------------- ")

      PerformTaskCmdReq(
        f.actionPerformedId,
        f.action.pInstId,
        f.action.taskId,
        f.curAction.get,
        f.task.get.activity,
        f.action.payLoad,
        f.action.participantId
      )
    }

    val mapToCommitActionCmdReq = Flow[ActionParams].map { f =>
      println(" mapToCommitActionCmdReq  ----------------- ")
      CommitActionCmdReq(f.action.pInstId, f.actionPerformedId, f.action.taskId, f.action.participantId, f.curAction.get, f.actionDate, f.action.comment)
    }

    val fi = Flow[(PerformTaskCmdRes, CommitActionCmdRes)].map {
      case (a: PerformTaskSuccess, b: CommitActionCmdSuccess) =>
        ActionCompleted()
    }

    val newTaskFilter  = Flow[ActionParams].filter(item => item.nexActivity match {
      case Some(_: DoneActivityFlow) =>
        println("--------Filter with DoneActivityFlow--------")
        false
      case  Some(_: NaActivityFlow) =>
        println("--------Filter with NaActivityFlow--------")
        false
      case Some(_) =>
        true
      case None =>
        false
    })

    val reRouteTaskFilter = Flow[ActionParams].filter(item => item.nexActivity match {
      case  Some(_: NaActivityFlow) =>
        if(item.action.action.equalsIgnoreCase("Assign") && item.newDestination.isDefined ) true
        else false
      case _ => false
    })

    val mapToCreateNewTaskCmdRequest = Flow[ActionParams].map { item =>

      println(" mapToCreateNewTaskCmdRequest  ----------------- ")

      val param = item
      val newActivity = param.nexActivity.get
      CreateNewTaskCmdRequest(
        param.action.pInstId,
        Task(param.newTaskId.get, newActivity.activity, newActivity.actionFlows.map(_.action), newActivity.participants.map(TaskDestination(_))),
        param.action.participantId
      )
    }

    val mapToReRouteTaskCmdRequest = Flow[ActionParams].map{ param =>
      ReRouteCmdReq(param.action.pInstId, param.action.taskId, param.newDestination.get)
    }

    val nexFlow = Flow[ActionParams].map { f =>

      f.flow.get.nextActivity(f.curAction.get, f.task.get.activity, ParticipantAccess(f.action.participantId)) match {
        case Right(activity) => f.copy(nexActivity = Some(activity), newTaskId = Some(UUID.randomUUID()))
      }
    }

    val createNewTask = processInstanceAggregateFlows.createNewTask.map {
      case CreateNewTaskCmdSuccess(pInstId, taskId, destList) => destList.map(AssignTaskCmdReq(_, taskId, pInstId))
    }.flatMapConcat(Source(_))


    bCast.out(0) ~> mapToPerformTaskCmdReq ~> processInstanceAggregateFlows.performTask.map {
      case a: PerformTaskSuccess => ActionCompleted()
    } ~> performTaskZip.in0

    bCast.out(1) ~> mapToCommitActionCmdReq ~> processInstanceAggregateFlows.commitAction.map {
      case a: CommitActionCmdSuccess => ActionCompleted()
    } ~> performTaskZip.in1


    //Next Task

    bCast.out(2) ~> nexFlow ~> taskCast.in

    taskCast.out(0) ~>    newTaskFilter ~>  mapToCreateNewTaskCmdRequest ~> createNewTask ~> participantAggregateFlows.assignTask.map {
      case _ : AssignTaskCmdSuccess => ActionCompleted()
    } ~> Sink.ignore

    taskCast.out(1)  ~> reRouteTaskFilter ~> mapToReRouteTaskCmdRequest ~> processInstanceAggregateFlows.reRoute.map {
      case ReRouteCmdSuccess(id, taskId, participantId) =>
        ActionCompleted()
      case _ =>
        ActionCompleted()
    }  ~> Sink.ignore


    FlowShape(bCast.in, performTaskZip.out)
  })

  private val testFlow = Flow.fromGraph(GraphDSL.create() { implicit b =>

    import GraphDSL.Implicits._

    val bCast = b.add(Broadcast[Int](3))
    val zipFlow = b.add(Zip[Int, Int])


    bCast.out(0) ~> zipFlow.in0
    bCast.out(1) ~> zipFlow.in1

    //zipFlow.out ~> Sink.ignore

    bCast.out(2) ~> Flow[Int].flatMapConcat(f => Source(List(f, 2, 3, 4, 5))).map { t =>
      println(s"process ${t}")
      t
    } ~> Sink.ignore

    FlowShape(bCast.in, zipFlow.out.map(f => f._2 + f._1).outlet)

  })


  private val createInstanceFlow: SourceQueueWithComplete[(CreatePInstCmdRequest, Promise[CreatePInstCmdResponse])] = Source
    .queue[(CreatePInstCmdRequest, Promise[CreatePInstCmdResponse])](10, OverflowStrategy.dropNew)
    .via(prepareCreateInst.zipPromise)
    .toMat(completePromiseSink)(Keep.left)
    .run()

  private val takeActionFlow: SourceQueueWithComplete[(TakeActionCmdRequest, Promise[ActionCompleted])] = Source
    .queue[(TakeActionCmdRequest, Promise[ActionCompleted])](10, OverflowStrategy.dropNew)
    .via(actionParamsFlowGraph.via(takeActionFlowGraph).zipPromise)
    .toMat(completePromiseSink)(Keep.left)
    .run()

  private val getPInstFlow: SourceQueueWithComplete[(GetPInstCmdRequest, Promise[GetPInstCmdResponse])] = Source
    .queue[(GetPInstCmdRequest, Promise[GetPInstCmdResponse])](100, OverflowStrategy.dropNew)
    .via(processInstanceAggregateFlows.getPInst.zipPromise)
    .toMat(completePromiseSink)(Keep.left)
    .run()


  private val testFoldFlow: SourceQueueWithComplete[(Int, Promise[Int])] = Source
    .queue[(Int, Promise[Int])](10, OverflowStrategy.dropNew)
    .via(testFlow.zipPromise)
    .toMat(completePromiseSink)(Keep.left)
    .run()

  def testFold(n: Int)(implicit ec: ExecutionContext) =
    offerToQueue(testFoldFlow)(n, Promise())

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
