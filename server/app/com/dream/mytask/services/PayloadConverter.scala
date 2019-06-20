package com.dream.mytask.services

import com.dream.common.{NonePayload, Payload}
import com.dream.mytask.shared.data.WorkflowData.{EditTicketPayloadJs, PayloadJs}
import com.dream.common.dto.ticket.TicketDto.EditTicketPayload

trait PayloadConverter {

  def convertPayload(payload: PayloadJs): Payload =
    payload match {
      case data: EditTicketPayloadJs => EditTicketPayload(data.payloadCode)
      case _ => NonePayload() //Throw exception
    }

}
