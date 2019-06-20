package com.dream.ticket

import java.util.UUID

import akka.actor.{Actor, ActorLogging, Props}
import com.dream.common.Protocol.{DefaultTaskPerformCmdResponse, TaskPerformCmdRequest}

object TicketHandler {

  final val serviceName = "ticket-task-handler"

  def prop = Props(new TicketHandler)


}

class TicketHandler extends Actor with ActorLogging {
  override def receive: Receive = {
    case req: TaskPerformCmdRequest => {
      println(s"--do perform task ${req}")
      sender() ! DefaultTaskPerformCmdResponse(UUID.randomUUID())
    }
  }
}
