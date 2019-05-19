package com.dream.mytask.shared.data

import java.util.UUID

import com.dream.mytask.shared.data.AccountData.ParticipantJson


object WorkflowData {

  case class FlowJson(id: String, name: String)

  sealed trait PayloadJs {
    def comment: Option[String]
  }

  case class EditTicketPayloadJs(test: String, override val comment: Option[String]) extends PayloadJs

  case class TicketStatusPayloadJs(status: String,  override val comment: Option[String]) extends PayloadJs

  case class AssignTicketPayloadJs(participantId: UUID, override val comment: Option[String]) extends PayloadJs

  case class CommentPayloadJs(override val comment: Option[String]) extends PayloadJs

  case class FlowInitDataJs(
    list: List[FlowJson],
    pcpList: List[ParticipantJson]
  )

}
