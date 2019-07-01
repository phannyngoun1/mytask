package com.dream.mytask.services

import java.util.UUID

import akka.actor.ActorSystem
import com.dream.mytask.models.User
import com.dream.mytask.services.TicketPayloadConverter._
import com.dream.mytask.shared.Api
import com.dream.mytask.shared.data.WorkflowData.PayloadJs
import com.dream.mytask.shared.data._

import com.dream.workflow.adaptor.journal.JournalReaderImpl
import com.dream.workflow.usecase.AccountAggregateUseCase.Protocol.{GetAccountCmdReq, GetAccountCmdSuccess, _}
import com.dream.workflow.usecase.ProcessInstanceAggregateUseCase.Protocol.TakeActionCmdRequest
import com.dream.workflow.usecase._

import scala.concurrent.{ExecutionContext, Future}


class ApiService(apiServiceResources: ApiServiceResources, user: User)(implicit val ec: ExecutionContext, implicit val system: ActorSystem)
  extends ApiServiceGlobal
    with PayloadConverter
    with ItemService
    with FlowService
    with AccountService
    with PInstanceService
    with TicketService {

  val readSideFlow = apiServiceResources.getItemReadModelFlow
  val flowReadModelFlow = apiServiceResources.getFlowReadModelFlow
  val accountReadModelFlow = apiServiceResources.getAccountReadModelFlow
  val participantReadModelFlows = apiServiceResources.getParticipantReadModelFlows
  val pInstanceReadModelFlows = apiServiceResources.getPInstanceReadModelFlows
  val flagReadModelFlows = apiServiceResources.getFlagReadModelFlows


  val localEntityAggregates = apiServiceResources.getLocalEntityAggregates

  val itemFlow = apiServiceResources.getItemAggregateFlows
  val workFlow = apiServiceResources.getWorkflowAggregateFlows
  val pInstFlow = apiServiceResources.getProcessInstanceAggregateFlows
  val accountFlow = apiServiceResources.getAccountAggregateFlows
  val participantFlow = apiServiceResources.getParticipantAggregateFlows
  val itemAggregateUseCase = apiServiceResources.getItemAggregateUseCase
  val workflowAggregateUseCase = apiServiceResources.getWorkflowAggregateUseCase
  val processInstance = apiServiceResources.getProcessInstanceAggregateUseCase
  val accountUseCase = apiServiceResources.getAccountAggregateUseCase
  val participantUseCase = apiServiceResources.getParticipantAggregateUseCase

  val ex = new ReadModelUseCase(
    readSideFlow,
    flowReadModelFlow,
    accountReadModelFlow,
    participantReadModelFlows,
    pInstanceReadModelFlows,
    flagReadModelFlows,
    new JournalReaderImpl(system)
  )

  ex.execute


  var curUser: Option[User] = None

  def fetchUser(user: User) = {
    curUser = Some(user)
  }


  override def welcomeMessage(smg: String): Future[String] = {
    Future.successful(s"welcome $smg - ${UUID.randomUUID()}")
  }

  override def getUser(id: String): Future[String] = {
    accountUseCase.getAccount(GetAccountCmdReq(UUID.fromString(id))).map {
      case res: GetAccountCmdSuccess => s"id: ${res.id}, name: ${res.name}, full name: ${res.fullName}, participant id: ${res.curParticipantId} "
      case _ => "Failed"
    }
  }

  override def getTasks(accId: String): Future[List[TaskItemJson]] = {

    val uuId = UUID.fromString(accId)


    accountUseCase.getTasks(GetTaskLisCmdReq(uuId)) map (_.map { f =>
      TaskItemJson(f.id.toString, f.pInstId.toString, f.participantId.toString, f.activity.name, List.empty ++ f.actions.map(a => ActionItemJson(a._1.name, a._2)))
    })
  }

  override def takeAction(pInstId: String, taskId: String, accId: String, participantId: String, actionName: String, payload: PayloadJs): Future[String] = {

    val pInstIdUUID = UUID.fromString(pInstId)
    val taskIdUUID = UUID.fromString(taskId)
    val participantUUI = UUID.fromString(participantId)

    processInstance.takeAction(TakeActionCmdRequest(pInstIdUUID, taskIdUUID, actionName, participantUUI, convertEditTicketPayload(payload), payload.comment)).map {
      case _ => "Completed"
    }
  }

  override def getApiServiceResources: ApiServiceResources = apiServiceResources
}