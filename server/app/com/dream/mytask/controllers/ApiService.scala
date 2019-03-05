package com.dream.mytask.controllers

import java.util.UUID

import akka.actor.ActorSystem
import com.dream.mytask.shared.Api
import com.dream.mytask.shared.data.{ActionItem, TaskItem}
import com.dream.workflow.adaptor.aggregate._
import com.dream.workflow.usecase._
import com.dream.workflow.usecase.AccountAggregateUseCase.Protocol.{GetAccountCmdReq, GetAccountCmdSuccess}
import com.dream.workflow.usecase.ProcessInstanceAggregateUseCase.Protocol.{CreatePInstCmdRequest, CreatePInstCmdSuccess, GetPInstCmdRequest, GetPInstCmdSuccess}
import com.dream.workflow.usecase.AccountAggregateUseCase.Protocol._

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
  val accountUseCase = new AccountAggregateUseCase(accountFlow,participantFlow, pInstFlow )
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

  override def getTasks(accId: String): Future[List[TaskItem]] = {

    val uuId = UUID.fromString(accId)

    accountUseCase.getTasks(GetTaskLisCmdReq(uuId)) map {
      case res: GetTaskListCmdSuccess => res.taskList.map( f => TaskItem(f.id, f.activity.name, f.actions.map(a => ActionItem(a.name))) )
      case _ => List.empty[TaskItem]
    }
  }

  override def createProcessInstance(): Future[String] = {

    processInstance.createPInst(CreatePInstCmdRequest(
      itemID = UUID.fromString("8c557884-0d50-4ba1-aa82-26b49b5be368"),
      by = UUID.fromString("8dbd6bf8-2f60-4e6e-8e3f-b374e060a940")
    )) map {
      case res: CreatePInstCmdSuccess => s"Process instance ${res.folio} created"
      case _ => "Failed"
    }
  }

  override def getProcessInstance(id: String): Future[String] = {
    val uuId = UUID.fromString(id)

    processInstance.getPInst(GetPInstCmdRequest(uuId))  map {
      case res: GetPInstCmdSuccess => res.folio
      case _ => s"Failed to fetch ${id}"
    }

  }
}