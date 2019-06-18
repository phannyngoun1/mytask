package com.dream.common.dto.ticket

import com.dream.common.Payload


object TicketDto {

  case class EditTicketPayload(
    payloadCode: Option[String] = Some("ticket-payload")
  ) extends Payload

}
