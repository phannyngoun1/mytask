package com.dream.mytask.services

import com.dream.common.{NonePayload, Payload}
import com.dream.common.dto.ticket.TicketDto.EditTicketPayload
import com.dream.mytask.shared.data.WorkflowData.{AssignTicketPayloadJs, EditTicketPayloadJs, PayloadJs}
import com.dream.ticket.domain.TicketDomain.AssignTicketPayload

object TicketPayloadConverter {

  def convertEditTicketPayload(payload: PayloadJs): Payload = {
    case AssignTicketPayloadJs(participantId, _ ) => AssignTicketPayload(participantId)
    case _ => NonePayload()
  }

}



