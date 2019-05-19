package com.dream.ticket.domain

import java.util.UUID

import com.dream.common.{Payload, ReRoutePayload}

object TicketDomain {

  trait TicketPayLoad extends Payload

  case class EditTicketPayload() extends TicketPayLoad

  case class AssignTicketPayload(participantId: UUID) extends TicketPayLoad with ReRoutePayload

}
