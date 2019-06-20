package com.dream.mytask.services

import com.dream.common.{NonePayload, Payload}
import com.dream.mytask.shared.data.WorkflowData.{AssignTicketPayloadJs, PayloadJs}
import com.dream.ticket.domain.TicketDomain.AssignTicketPayload

object TicketPayloadConverter {

  def convertEditTicketPayload(payload: PayloadJs): Payload =
    payload match {
      case AssignTicketPayloadJs(payloadCode ,participantId, _ ) => AssignTicketPayload(payloadCode = payloadCode ,participantId)
      case _ => NonePayload()
    }

}



