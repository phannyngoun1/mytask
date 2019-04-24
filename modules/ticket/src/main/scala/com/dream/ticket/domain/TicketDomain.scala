package com.dream.ticket.domain

import com.dream.common.Payload

object TicketDomain {

  trait TicketPayLoad extends Payload

  case class EditTicketPayload() extends TicketPayLoad

}
