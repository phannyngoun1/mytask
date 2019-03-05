package com.dream.workflow.usecase

import java.util.UUID

import akka.NotUsed
import akka.actor.ActorSystem
import com.dream.common.UseCaseSupport
import com.dream.common.domain.ResponseError
import com.dream.workflow.domain.Task
import com.dream.workflow.usecase.AccountAggregateUseCase.Protocol.GetTaskLisCmdReq
import com.dream.workflow.usecase.ParticipantAggregateUseCase.Protocol.{GetAssignedTaskCmdReq, GetAssignedTaskCmdSuccess}
import com.dream.workflow.usecase.ProcessInstanceAggregateUseCase.Protocol.{GetTaskCmdReq, GetTaskCmdSuccess}
import com.dream.workflow.usecase.port.{AccountAggregateFlows, ParticipantAggregateFlows, ProcessInstanceAggregateFlows}

import scala.concurrent.{ExecutionContext, Future, Promise}

object AccountAggregateUseCase {

  object Protocol {

    sealed trait AccountCmdResponse

    sealed trait AccountCmdRequest

    case class CreateAccountCmdReq(
      id: UUID,
      name: String,
      fullName: String,
      participantId: Option[UUID] = None
    ) extends AccountCmdRequest

    sealed trait CreateAccountCmdRes extends AccountCmdResponse

    case class CreateAccountCmdSuccess(
      id: UUID
    ) extends CreateAccountCmdRes


    case class CreateAccountCmdFailed(
      error: ResponseError
    ) extends CreateAccountCmdRes

    case class GetAccountCmdReq(id: UUID) extends AccountCmdRequest

    sealed trait GetAccountCmdRes extends AccountCmdResponse

    case class GetAccountCmdSuccess(
      id: UUID,
      name: String,
      fullName: String,
      curParticipantId: Option[UUID] = None
    ) extends GetAccountCmdRes

    case class GetAccountCmdFailed(responseError: ResponseError) extends GetAccountCmdRes

    case class AssignParticipantCmdReq(id: UUID, participantId: UUID) extends AccountCmdRequest

    sealed trait AssignParticipantCmdRes extends AccountCmdResponse

    case class AssignParticipantCmdSuccess(id: UUID) extends AssignParticipantCmdRes

    case class AssignParticipantCmdFailed(responseError: ResponseError) extends AssignParticipantCmdRes

    case class GetParticipantCmdReq(accId: UUID) extends AccountCmdRequest

    sealed trait GetParticipantCmdRes extends AccountCmdResponse
    case class GetParticipantCmdSuccess(participantIds: List[UUID]) extends GetParticipantCmdRes
    case class GetParticipantCmdFailed(responseError: ResponseError) extends GetParticipantCmdRes

    case class GetTaskLisCmdReq(id: UUID) extends  AccountCmdRequest
    trait GetTaskListCmdRes extends  AccountCmdResponse
    case class GetTaskListCmdSuccess(taskList: List[Task]) extends GetTaskListCmdRes
    case class GetTaskListCmdFailed(responseError: ResponseError) extends AccountCmdResponse

  }
}

class AccountAggregateUseCase(
  flow: AccountAggregateFlows,
  partFlow: ParticipantAggregateFlows,
  pInstFlow: ProcessInstanceAggregateFlows)
  (implicit system: ActorSystem) extends UseCaseSupport {

  import AccountAggregateUseCase.Protocol._
  import UseCaseSupport._
  import akka.stream._
  import akka.stream.scaladsl._

  implicit val mat: Materializer = ActorMaterializer()

  private val bufferSize: Int = 10


  private val getTaskFlow: Flow[GetTaskLisCmdReq, List[Task], NotUsed] =
    Flow[GetTaskLisCmdReq]
    .map(req => GetParticipantCmdReq(req.id))
      .via(flow.getParticipant.map {
        case GetParticipantCmdSuccess(partIds) => partIds
      })
    .flatMapConcat(parts => Source(parts.map(GetAssignedTaskCmdReq(_))))
    .via(partFlow.getAssignedTasks.map {
      case GetAssignedTaskCmdSuccess(assignedTasks) => assignedTasks
      case _ => List.empty
    })
    .flatMapConcat(assignedTasks=> Source(assignedTasks.map(GetTaskCmdReq(_))))
    .via(pInstFlow.getTask)
    .fold(List.empty[Task])( ((m, e) => (
      e match {
      case GetTaskCmdSuccess(task) => Some(task)
      case _ => None
    }).map( _ :: m ).getOrElse(m)))


  private val getTaskQueue: SourceQueueWithComplete[(GetTaskLisCmdReq, Promise[List[Task]])] =
  Source.queue[(GetTaskLisCmdReq, Promise[List[Task]])](bufferSize, OverflowStrategy.dropNew)
    .via(getTaskFlow.zipPromise)
    .toMat(completePromiseSink)(Keep.left)
    .run()

  private val createAccountQueue: SourceQueueWithComplete[(CreateAccountCmdReq, Promise[CreateAccountCmdRes])] =
    Source.queue[(CreateAccountCmdReq, Promise[CreateAccountCmdRes])](bufferSize, OverflowStrategy.dropNew)
      .via(flow.create.zipPromise)
      .toMat(completePromiseSink)(Keep.left)
      .run()

  private val getAccountQueue: SourceQueueWithComplete[(GetAccountCmdReq, Promise[GetAccountCmdRes])] =
    Source.queue[(GetAccountCmdReq, Promise[GetAccountCmdRes])](bufferSize, OverflowStrategy.dropNew)
      .via(flow.get.zipPromise)
      .toMat(completePromiseSink)(Keep.left)
      .run()

  private val assignParticipantQueue: SourceQueueWithComplete[(AssignParticipantCmdReq, Promise[AssignParticipantCmdRes])] =
    Source.queue[(AssignParticipantCmdReq, Promise[AssignParticipantCmdRes])](bufferSize, OverflowStrategy.dropNew)
      .via(flow.assignParticipant.zipPromise)
      .toMat(completePromiseSink)(Keep.left)
      .run()

  def createAccount(req: CreateAccountCmdReq)(implicit ec: ExecutionContext): Future[CreateAccountCmdRes] =
    offerToQueue(createAccountQueue)(req, Promise())

  def getAccount(req: GetAccountCmdReq)(implicit ec: ExecutionContext): Future[GetAccountCmdRes] =
    offerToQueue(getAccountQueue)(req, Promise())

  def assignParticipant(req: AssignParticipantCmdReq)(implicit ec: ExecutionContext): Future[AssignParticipantCmdRes] =
    offerToQueue(assignParticipantQueue)(req, Promise())

  def getTasks(req: GetTaskLisCmdReq)(implicit ec: ExecutionContext): Future[List[Task]] =
    offerToQueue(getTaskQueue)(req, Promise())


}
