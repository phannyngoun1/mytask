package com.dream.workflow.adaptor.aggregate

import java.util.UUID

import akka.actor.{Actor, ActorLogging, Props}
import com.dream.common.{BaseAction, BaseActivity, Payload}
import com.dream.common.Protocol.{DefaultTaskPerformCmdResponse, TaskPerformCmdRequest}
import DefaultHandler._
import com.dream.ticket.TicketHandler

object DefaultHandler {

  final val serviceName = "default-task-handler"

  def prop = Props(new TicketHandler)

  sealed trait TicketCmdResponse

  sealed trait TicketCmdRequest extends TaskPerformCmdRequest

  case class PerformDefaultCmdRequest(
    val taskId: UUID,
    val action: BaseAction,
    val activity: BaseActivity,
    val payLoad: Payload
  ) extends TicketCmdRequest
}

class DefaultHandler extends Actor with ActorLogging {
  override def receive: Receive = {
    case cmd: PerformDefaultCmdRequest =>
      sender() ! DefaultTaskPerformCmdResponse(UUID.randomUUID())
  }
}
