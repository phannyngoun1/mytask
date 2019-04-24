package com.dream.mytask.services

import com.dream.common.{NonePayload, Payload}
import com.dream.mytask.shared.data.WorkflowData.{EditTicketPayloadJs, PayloadJs}
import TicketPayloadConverter._

trait PayloadConverter {

  def convertPayload(payload: PayloadJs): Payload =
    payload match {
      case data: EditTicketPayloadJs => data
      case _ => NonePayload() //Throw exception
    }

}
