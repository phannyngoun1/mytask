package com.dream.mytask.shared.data

import java.util.UUID

import com.dream.mytask.shared.data.AccountData.ParticipantJson


object WorkflowData {

  case class FlowJson(id: String, name: String)

  sealed trait PayloadJs

  case class EditTicketPayloadJs(test: String) extends PayloadJs

  case class AssignTicketPayloadJs(participantId: UUID, status: String,  comment: Option[String]) extends PayloadJs

  case class FlowInitDataJs(
    list: List[FlowJson],
    pcpList: List[ParticipantJson]
  )

}
