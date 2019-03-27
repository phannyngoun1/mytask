package com.dream.workflow.usecase

import java.util.UUID

import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl._
import com.dream.common.domain.ResponseError
import com.dream.workflow.domain.{Flow => WFlow, _}
import com.dream.workflow.entity.processinstance.ProcessInstanceProtocol.{CreatePInstCmdRequest => CreateInst}
import com.dream.workflow.usecase.ItemAggregateUseCase.Protocol.{GetItemCmdRequest, GetItemCmdSuccess}
import com.dream.workflow.usecase.ParticipantAggregateUseCase.Protocol.AssignTaskCmdReq
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

    sealed trait CreatePInstCmdResponse

    case class CreatePInstCmdSuccess(
      folio: String
    ) extends CreatePInstCmdResponse

    case class CreatePInstCmdFailed(error: ResponseError) extends CreatePInstCmdResponse

    case class GetPInstCmdRequest(id: UUID) extends ProcessInstanceCmdRequest

    sealed trait GetPInstCmdResponse extends ProcessInstanceCmdResponse

    case class GetPInstCmdSuccess(id: UUID, folio: String) extends GetPInstCmdResponse

    case class GetPInstCmdFailed(error: ResponseError) extends GetPInstCmdResponse

    case class GetTaskCmdReq(assignedTask: AssignedTask) extends ProcessInstanceCmdRequest

    sealed trait GetTaskCmdRes extends ProcessInstanceCmdResponse

    case class GetTaskCmdSuccess(taskDto: TaskDto) extends GetTaskCmdRes

    case class GetTaskCmdFailed(error: ResponseError) extends GetTaskCmdRes

    case class TakeActionCmdRequest(pInstId: UUID, taskId: UUID, action: BaseAction, participantId: UUID, payLoad: PayLoad)  extends ProcessInstanceCmdRequest

    trait TakeActionCmdResponse

    case class TakeActionCmdSuccess() extends TakeActionCmdResponse
    case class TakeActionCmdFailed(error: ResponseError) extends TakeActionCmdResponse


    case class PerformTaskCmdReq(
      pInstId: UUID,
      activity: BaseActivity,
    ) extends ProcessInstanceCmdRequest

    sealed trait PerformTaskCmdRes extends ProcessInstanceCmdResponse

    case class PerformTaskSuccess() extends PerformTaskCmdRes
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
          task =  Task(
            id =  UUID.randomUUID(),
            activity =  nextFlow.activity,
            actions = nextFlow.actionFlows.map(_.action),
            nextFlow.participants.map(TaskDestination(_))
          )
        )
      }
    )

    broadcast.out(0) ~> convertToGetItem ~> itemAggregateFlows.getItem.map {
      case res: GetItemCmdSuccess => GetWorkflowCmdRequest(res.workflowId)
    } ~> workflowAggregateFlows.getWorkflow.map {
      case GetWorkflowCmdSuccess(workflow) => workflow
    } ~> createInstZip.in0

    broadcast.out(1) ~> createInstZip.in1

    val createPrepareB = b.add(Broadcast[CreateInst](3))
    val convertToTaskCmdRequestFlow = Flow[CreateInst].map(p => PerformTaskCmdReq(p.id, p.task.activity))

    //TODO: adding real tasks

    val assignTaskCmdFlow = Flow[CreateInst].flatMapConcat(p => Source(p.destIds.map(dest =>  AssignTaskCmdReq(dest ,p.task.id, p.id))))

    val out = createInstZip.out ~> convertToCreatePInstCmdReq ~> createPrepareB ~> processInstanceAggregateFlows.createInst
    createPrepareB ~> convertToTaskCmdRequestFlow ~> processInstanceAggregateFlows.performTask ~> Sink.ignore
    createPrepareB ~> assignTaskCmdFlow ~> participantAggregateFlows.assignTask ~> Sink.ignore

    FlowShape(broadcast.in, out.outlet)
  })


  private val takeActionFlowGraph = Flow.fromGraph(GraphDSL.create() { implicit b =>

    import GraphDSL.Implicits._
    val broadcast = b.add(Broadcast[TakeActionCmdRequest](2))

    val mapToGetTaskReq = Flow[TakeActionCmdRequest].map(item => GetTaskCmdReq(AssignedTask(item.taskId, item.participantId)))

    broadcast.out(0) ~> mapToGetTaskReq ~> processInstanceAggregateFlows.getTask

    val zip = b.add(Zip[String, String])
    FlowShape(broadcast.in, zip.out)

  })

  private val createInstanceFlow: SourceQueueWithComplete[(CreatePInstCmdRequest, Promise[CreatePInstCmdResponse])] = Source
    .queue[(CreatePInstCmdRequest, Promise[CreatePInstCmdResponse])](10, OverflowStrategy.dropNew)
    .via(prepareCreateInst.zipPromise)
    .toMat(completePromiseSink)(Keep.left)
    .run()

  private val getPInstFlow: SourceQueueWithComplete[(GetPInstCmdRequest, Promise[GetPInstCmdResponse])] = Source
    .queue[(GetPInstCmdRequest, Promise[GetPInstCmdResponse])](10, OverflowStrategy.dropNew)
    .via(processInstanceAggregateFlows.getPInst.zipPromise)
    .toMat(completePromiseSink)(Keep.left)
    .run()

  def createPInst(request: CreatePInstCmdRequest)(implicit ec: ExecutionContext): Future[CreatePInstCmdResponse] = {
    offerToQueue(createInstanceFlow)(request, Promise())
  }

  def getPInst(request: GetPInstCmdRequest)(implicit ec: ExecutionContext): Future[GetPInstCmdResponse] =
    offerToQueue(getPInstFlow)(request, Promise())

  def list: Future[List[ProcessInstanceDto]] = {
    val sumSink =  Sink.fold[List[ProcessInstanceDto], ProcessInstanceDto](List.empty[ProcessInstanceDto])( (m ,e) =>  e :: m )
    Source.fromPublisher(pInstanceReadModelFlows.list).toMat(sumSink)(Keep.right).run()
  }



}
