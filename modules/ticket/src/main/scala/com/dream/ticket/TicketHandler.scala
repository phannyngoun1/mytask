package com.dream.ticket

import java.util.UUID

import akka.actor.{Actor, ActorLogging, Props}
import com.dream.common.Protocol.{DefaultTaskPerformCmdResponse, TaskPerformCmdRequest}
import com.dream.common.{BaseAction, BaseActivity, Payload}
import com.dream.ticket.TicketHandler.Protocol.{PerformTicketCmdRequest, TicketCmdResponse}

object TicketHandler {

  final val serviceName = "ticket-task-handler"

  def prop = Props(new TicketHandler)

  object Protocol {

    sealed trait TicketCmdResponse

    sealed trait TicketCmdRequest extends TaskPerformCmdRequest

    case class PerformTicketCmdRequest(
      val taskId: UUID,
      val action: BaseAction,
      val activity: BaseActivity,
      val payLoad: Payload
    ) extends TicketCmdRequest
  }

}

class TicketHandler extends Actor with ActorLogging {
  override def receive: Receive = {
    case req: PerformTicketCmdRequest => {

      println(s"--do perform task ${req}")

      sender() ! DefaultTaskPerformCmdResponse(UUID.randomUUID())
    }
  }
}
