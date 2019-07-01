package com.dream.workflow.usecase

import java.util.UUID

import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl._
import com.dream.common.domain.ResponseError
import com.dream.common.dto.workflow.Account.ParticipantDto
import com.dream.workflow.domain.AssignedTask
import com.dream.workflow.usecase.AccountAggregateUseCase.Protocol._
import com.dream.workflow.usecase.port._

import scala.concurrent.{ExecutionContext, Future, Promise}

object ParticipantAggregateUseCase {

  object Protocol {

    sealed trait ParticipantCmdResponse

    sealed trait ParticipantCmdRequest

    sealed trait CreateParticipantCmdRes

    sealed trait GetParticipantCmdRes extends ParticipantCmdResponse

    sealed trait AssignTaskCmdRes extends ParticipantCmdResponse

    sealed trait GetAssignedTaskCmdRes extends ParticipantCmdResponse

    case class CreateParticipantCmdReq(
      id: UUID,
      accountId: UUID,
      teamId: UUID,
      departmentId: UUID,
      propertyId: UUID
    ) extends ParticipantCmdRequest

    case class CreateParticipantCmdSuccess(
      id: UUID
    ) extends CreateParticipantCmdRes

    case class CreateParticipantCmdFailed(error: ResponseError) extends CreateParticipantCmdRes

    case class GetParticipantCmdReq(
      id: UUID
    ) extends ParticipantCmdRequest

    case class GetParticipantCmdSuccess(
      id: UUID,
      accountId: UUID,
      tasks: List[AssignedTask] = List.empty
    ) extends GetParticipantCmdRes

    case class GetParticipantCmdFailed(error: ResponseError) extends GetParticipantCmdRes

    case class AssignTaskCmdReq(
      id: UUID,
      taskId: UUID,
      pInstId: UUID,
    ) extends ParticipantCmdRequest

    case class AssignTaskCmdSuccess(id: UUID) extends AssignTaskCmdRes

    case class AssignTaskCmdFailed(error: ResponseError) extends AssignTaskCmdRes

    case class GetAssignedTaskCmdReq(
      id: UUID
    ) extends ParticipantCmdRequest

    case class GetAssignedTaskCmdSuccess(id: UUID, assignedTasks: List[AssignedTask]) extends GetAssignedTaskCmdRes

    case class GetAssignedTaskCmdFailed(error: ResponseError) extends GetAssignedTaskCmdRes

  }

}

class ParticipantAggregateUseCase(
  participantAggregateFlows: ParticipantAggregateFlows,
  accountAggregateFlows: AccountAggregateFlows,
  participantReadModelFlows: ParticipantReadModelFlows
)(implicit system: ActorSystem) extends UseCaseSupport {

  import ParticipantAggregateUseCase.Protocol._
  import UseCaseSupport._


  val decider: Supervision.Decider = {
    case _ => Supervision.Restart
  }

  implicit val mat = ActorMaterializer(
    ActorMaterializerSettings(system)
      .withSupervisionStrategy(decider)
  )

  private val bufferSize: Int = 10
  private val createParticipantGraph = Flow.fromGraph(GraphDSL.create() { implicit b =>
    import GraphDSL.Implicits._

    val broadcast = b.add(Broadcast[CreateParticipantCmdReq](2))
    val zip = b.add(Zip[CreateParticipantCmdRes, AssignParticipantCmdRes])
    val convertToAssignReq = Flow[CreateParticipantCmdReq].map(req => AssignParticipantCmdReq(req.accountId, req.id))

    broadcast.out(0) ~> participantAggregateFlows.create ~> zip.in0
    broadcast.out(1) ~> convertToAssignReq ~> accountAggregateFlows.assignParticipant ~> zip.in1

    //TODO: workaround, need to be fixed
    val output = zip.out map {
      case (a: CreateParticipantCmdRes, b: AssignParticipantCmdSuccess) => a
    }
    FlowShape(broadcast.in, output.outlet)
  })
  private val createParticipantQueue: SourceQueueWithComplete[(CreateParticipantCmdReq, Promise[CreateParticipantCmdRes])] =
    Source.queue[(CreateParticipantCmdReq, Promise[CreateParticipantCmdRes])](bufferSize, OverflowStrategy.dropNew)
      .via(createParticipantGraph.zipPromise)
      .toMat(completePromiseSink)(Keep.left)
      .run()
  private val getParticipantQueue: SourceQueueWithComplete[(GetParticipantCmdReq, Promise[GetParticipantCmdRes])] =
    Source.queue[(GetParticipantCmdReq, Promise[GetParticipantCmdRes])](bufferSize, OverflowStrategy.dropNew)
      .via(participantAggregateFlows.get.zipPromise)
      .toMat(completePromiseSink)(Keep.left)
      .run()
  private val assignTaskQueue: SourceQueueWithComplete[(AssignTaskCmdReq, Promise[AssignTaskCmdRes])] =
    Source.queue[(AssignTaskCmdReq, Promise[AssignTaskCmdRes])](bufferSize, OverflowStrategy.dropNew)
      .via(participantAggregateFlows.assignTask.zipPromise)
      .toMat(completePromiseSink)(Keep.left)
      .run()

  def createParticipant(req: CreateParticipantCmdReq)(implicit ec: ExecutionContext): Future[CreateParticipantCmdRes] =
    offerToQueue(createParticipantQueue)(req, Promise())

  def getParticipant(req: GetParticipantCmdReq)(implicit ec: ExecutionContext): Future[GetParticipantCmdRes] =
    offerToQueue(getParticipantQueue)(req, Promise())

  def assignTask(req: AssignTaskCmdReq)(implicit ec: ExecutionContext): Future[AssignTaskCmdRes] =
    offerToQueue(assignTaskQueue)(req, Promise())

  def list: Future[List[ParticipantDto]] = {
    val sumSink = Sink.fold[List[ParticipantDto], ParticipantDto](List.empty[ParticipantDto])((m, e) => e :: m)
    Source.fromPublisher(participantReadModelFlows.list).toMat(sumSink)(Keep.right).run()
  }

}
