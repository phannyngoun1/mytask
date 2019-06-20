package com.dream.workflow.adaptor.aggregate

import akka.actor.{Actor, ActorContext}
import com.dream.common.Protocol.{DefaultTaskPerformCmdRequest, TaskPerformCmdRequest}
import com.dream.ticket.TicketHandler
import com.dream.workflow.entity.account.AccountEntity
import com.dream.workflow.entity.account.AccountProtocol.AccountCmdRequest
import com.dream.workflow.entity.item.ItemEntity
import com.dream.workflow.entity.item.ItemProtocol.ItemCmdRequest
import com.dream.workflow.entity.participant.ParticipantEntity
import com.dream.workflow.entity.participant.ParticipantProtocol.ParticipantCmdRequest
import com.dream.workflow.entity.processinstance.ProcessInstanceEntity
import com.dream.workflow.entity.processinstance.ProcessInstanceProtocol.ProcessInstanceCmdRequest
import com.dream.workflow.entity.workflow.WorkflowEntity
import com.dream.workflow.entity.workflow.WorkflowProtocol.WorkFlowCmdRequest


trait AggregatesLookup {

  implicit def context: ActorContext

  def forwardToEntityAggregate: Actor.Receive = {
    case cmd: ProcessInstanceCmdRequest =>
      context
        .child(ProcessInstanceEntity.name(cmd.id))
        .fold(
          context.actorOf(ProcessInstanceEntity.prop, ProcessInstanceEntity.name(cmd.id )) forward cmd
        )(_ forward cmd)

    case cmd: WorkFlowCmdRequest =>
      context
        .child(WorkflowEntity.name(cmd.id))
        .fold(
          context.actorOf(WorkflowEntity.prop, WorkflowEntity.name(cmd.id)) forward cmd
        )(_ forward cmd)
    case cmd: ItemCmdRequest =>
      context
      .child(ItemEntity.name(cmd.id))
      .fold (
        context.actorOf(ItemEntity.prop, ItemEntity.name(cmd.id)) forward cmd
      )(_ forward cmd)

    case cmd: AccountCmdRequest =>
      context
        .child(AccountEntity.name(cmd.id))
        .fold(
          context.actorOf(AccountEntity.prop, AccountEntity.name(cmd.id)) forward cmd
        )(_ forward cmd)

    case cmd: ParticipantCmdRequest =>
      context
        .child(ParticipantEntity.name(cmd.id))
        .fold(
          context.actorOf(ParticipantEntity.prop, ParticipantEntity.name(cmd.id)) forward cmd
        )(_ forward cmd)

    case cmd: TaskPerformCmdRequest =>
      println( s"gateway ${cmd}" )
      forwardTask(cmd)

  }

  private def forwardTask(cmd: TaskPerformCmdRequest) =

    cmd.payLoad.payloadCode match {
      case Some(payloadCode) =>
        if(payloadCode.contains("ticket-payload")) {
          val ticketCmd = DefaultTaskPerformCmdRequest(cmd.taskId, cmd.action, cmd.activity, cmd.payLoad)
          context
            .child(TicketHandler.serviceName)
            .fold(
              context.actorOf(TicketHandler.prop, TicketHandler.serviceName) forward ticketCmd
            )(_ forward ticketCmd)
        }

      case _ =>

        val defaultCmd = DefaultTaskPerformCmdRequest(cmd.taskId, cmd.action, cmd.activity, cmd.payLoad)
        context
          .child(DefaultHandler.serviceName)
          .fold(
            context.actorOf(DefaultHandler.prop, DefaultHandler.serviceName) forward defaultCmd
          )(_ forward defaultCmd)
    }

}
