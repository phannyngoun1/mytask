package com.dream.mytask.controllers

import java.util.UUID

import akka.actor.ActorSystem
import com.dream.mytask.shared.Api
import com.dream.workflow.adaptor.aggregate._
import com.dream.workflow.usecase.AccountAggregateUseCase.Protocol.{GetAccountCmdReq, GetAccountCmdSuccess}
import com.dream.workflow.usecase._
import javax.inject.{Inject, Singleton}

import scala.concurrent.ExecutionContext


@Singleton
class ApiService @Inject()(implicit ec: ExecutionContext) extends Api {

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

  override def welcomeMessage(smg: String): String =  {
    s"welcome $smg - ${UUID.randomUUID()}"
  }

  override def getUser(id: String): String = {

    accountUseCase.getAccount(GetAccountCmdReq(UUID.fromString(id))).map {
      case res: GetAccountCmdSuccess => s"${res.curParticipantId}"
      case _ => "Failed"
    }

    ""

  }
}