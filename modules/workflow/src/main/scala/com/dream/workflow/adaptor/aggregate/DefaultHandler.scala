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


}

class DefaultHandler extends Actor with ActorLogging {
  override def receive: Receive = {
    case cmd: TaskPerformCmdRequest =>
      sender() ! DefaultTaskPerformCmdResponse(UUID.randomUUID())
    case _  => sender() ! DefaultTaskPerformCmdResponse(UUID.randomUUID())
  }
}
