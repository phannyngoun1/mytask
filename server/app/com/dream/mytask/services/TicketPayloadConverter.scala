package com.dream.mytask.services

import com.dream.common.Payload
import com.dream.common.dto.ticket.TicketDto.EditTicketPayload
import com.dream.mytask.shared.data.WorkflowData.{EditTicketPayloadJs, PayloadJs}

object TicketPayloadConverter {

  implicit def convertEditTicketPayload(payload: EditTicketPayloadJs) = EditTicketPayload()

}



