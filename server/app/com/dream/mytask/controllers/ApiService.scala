package com.dream.mytask.controllers

import java.util.UUID

import akka.actor.ActorSystem
import com.dream.mytask.shared.Api
import com.dream.mytask.shared.data.Task
import com.dream.workflow.adaptor.aggregate._
import com.dream.workflow.usecase._
import com.dream.workflow.usecase.AccountAggregateUseCase.Protocol.{GetAccountCmdReq, GetAccountCmdSuccess}

import scala.concurrent.{ExecutionContext, Future}


class ApiService(login: UUID)(implicit ec: ExecutionContext) extends Api {

  implicit val system: ActorSystem = ActorSystem("ticket-system")

  val localEntityAggregates = system.actorOf(LocalEntityAggregates.props, LocalEntityAggregates.name)
  val itemFlow = new ItemAggregateFlowsImpl(localEntityAggregates)
  val workFlow = new WorkflowAggregateFlowsImpl(localEntityAggregates)
  val pInstFlow = new ProcessInstanceAggregateFlowsImpl(localEntityAggregates)
  val accountFlow = new AccountAggregateFlowsImpl(localEntityAggregates)
  val participantFlow = new ParticipantAggregateFlowsImpl(localEntityAggregates)
  val itemAggregateUseCase = new ItemAggregateUseCase(itemFlow)
  val workflowAggregateUseCase = new WorkflowAggregateUseCase(workFlow)
  val processInstance = new ProcessInstanceAggregateUseCase(pInstFlow, workFlow, itemFlow, participantFlow)
  val accountUseCase = new AccountAggregateUseCase(accountFlow)
  val participantUseCase = new ParticipantAggregateUseCase(participantFlow)


  override def welcomeMessage(smg: String): Future[String] = {
    Future.successful(s"welcome $smg - ${UUID.randomUUID()}")
  }

  override def getUser(id: String): Future[String] = {
    accountUseCase.getAccount(GetAccountCmdReq(UUID.fromString(id))).map {
      case res: GetAccountCmdSuccess => s"id: ${res.id}, name: ${res.name}, full name: ${res.fullName}, participant id: ${res.curParticipantId} "
      case _ => "Failed"
    }

  }

  override def getTasks(accId: String): Future[List[Task]] = {
    Future.successful(
      List(Task(UUID.randomUUID(), UUID.randomUUID()))
    )
  }
}