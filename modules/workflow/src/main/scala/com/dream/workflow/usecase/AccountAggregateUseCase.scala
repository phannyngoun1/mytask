package com.dream.workflow.usecase

import java.util.UUID

import akka.NotUsed
import akka.actor.ActorSystem
import com.dream.common.{Activity, BaseAction}
import com.dream.common.domain.ResponseError
import com.dream.workflow.domain.Account.AccountDto
import com.dream.workflow.domain.{AssignedTask, Task, TaskDto}
import com.dream.workflow.usecase.ParticipantAggregateUseCase.Protocol.{GetAssignedTaskCmdReq, GetAssignedTaskCmdSuccess}
import com.dream.workflow.usecase.ProcessInstanceAggregateUseCase.Protocol.{GetTaskCmdReq, GetTaskCmdSuccess}
import com.dream.workflow.usecase.WorkflowAggregateUseCase.Protocol.{GetTaskActionCmdReq, GetTaskActionCmdSuccess}
import com.dream.workflow.usecase.port.{AccountAggregateFlows, AccountReadModelFlow, ParticipantAggregateFlows, ProcessInstanceAggregateFlows, WorkflowAggregateFlows}

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
  accFlow: AccountAggregateFlows,
  partFlow: ParticipantAggregateFlows,
  pInstFlow: ProcessInstanceAggregateFlows,
  accReadModelFlow: AccountReadModelFlow,
  workflow: WorkflowAggregateFlows
)(implicit system: ActorSystem) extends UseCaseSupport {

  import AccountAggregateUseCase.Protocol._
  import UseCaseSupport._
  import akka.stream._
  import akka.stream.scaladsl._

  val decider: Supervision.Decider = {
    case _ => Supervision.resume
  }

  implicit val mat = ActorMaterializer(
    ActorMaterializerSettings(system)
      .withSupervisionStrategy(decider)
  )

  private val bufferSize: Int = 10

  private val getTaskFlow: Flow[GetTaskLisCmdReq, TaskDto, NotUsed] =
    Flow[GetTaskLisCmdReq]
      .map(req => GetParticipantCmdReq(req.id))
      .via(accFlow.getParticipant.map {
        case GetParticipantCmdSuccess(partIds) => partIds
      })
      .flatMapConcat(parts => Source(parts.map(GetAssignedTaskCmdReq(_))))
      .via(partFlow.getAssignedTasks.map {
        case res : GetAssignedTaskCmdSuccess => res
        case _ => GetAssignedTaskCmdSuccess(UUID.randomUUID(), List[AssignedTask]())
      })
      .flatMapConcat(f=> Source(f.assignedTasks.map(GetTaskCmdReq(f.id, _))))
      .via(pInstFlow.getTask)
      .map ( _ match {
        case GetTaskCmdSuccess(task) => task
        case _ => TaskDto(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID() , Activity("test"), Map.empty, false)
      })
    .filter(_.active)
    .map(GetTaskActionCmdReq)
    .via(workflow.getTaskActions)
    .map{
      case GetTaskActionCmdSuccess(task) => task
    }

  // -----------------------------

  private val foldTasks = Sink.fold[List[TaskDto], TaskDto](List.empty[TaskDto])( (m ,e) => if(e.active && e.isOwner)  e :: m else m )

  def getTasks(req: GetTaskLisCmdReq)(implicit ec: ExecutionContext): Future[List[TaskDto]]  =
    Source.single(req).via(getTaskFlow).toMat(foldTasks)(Keep.right).run()

  private val createAccountQueue: SourceQueueWithComplete[(CreateAccountCmdReq, Promise[CreateAccountCmdRes])] =
    Source.queue[(CreateAccountCmdReq, Promise[CreateAccountCmdRes])](bufferSize, OverflowStrategy.dropNew)
      .via(accFlow.create.zipPromise)
      .toMat(completePromiseSink)(Keep.left)
      .run()

  private val getAccountQueue: SourceQueueWithComplete[(GetAccountCmdReq, Promise[GetAccountCmdRes])] =
    Source.queue[(GetAccountCmdReq, Promise[GetAccountCmdRes])](bufferSize, OverflowStrategy.dropNew)
      .via(accFlow.get.zipPromise)
      .toMat(completePromiseSink)(Keep.left)
      .run()

  private val assignParticipantQueue: SourceQueueWithComplete[(AssignParticipantCmdReq, Promise[AssignParticipantCmdRes])] =
    Source.queue[(AssignParticipantCmdReq, Promise[AssignParticipantCmdRes])](bufferSize, OverflowStrategy.dropNew)
      .via(accFlow.assignParticipant.zipPromise)
      .toMat(completePromiseSink)(Keep.left)
      .run()

  def createAccount(req: CreateAccountCmdReq)(implicit ec: ExecutionContext): Future[CreateAccountCmdRes] =
    offerToQueue(createAccountQueue)(req, Promise())

  def getAccount(req: GetAccountCmdReq)(implicit ec: ExecutionContext): Future[GetAccountCmdRes] =
    offerToQueue(getAccountQueue)(req, Promise())

  def assignParticipant(req: AssignParticipantCmdReq)(implicit ec: ExecutionContext): Future[AssignParticipantCmdRes] =
    offerToQueue(assignParticipantQueue)(req, Promise())

  def list: Future[List[AccountDto]] = {
    val sumSink =  Sink.fold[List[AccountDto], AccountDto](List.empty[AccountDto])( (m ,e) =>  e :: m )
    Source.fromPublisher(accReadModelFlow.list).toMat(sumSink)(Keep.right).run()
  }

}
