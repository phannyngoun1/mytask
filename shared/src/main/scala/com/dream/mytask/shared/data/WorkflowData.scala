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
    workflowTemplateList: List[WorkflowTemplateJs],
    pcpList: List[ParticipantJson]
  )

  case class ActionJs(name: String, actionType: String)
  case class ActivityJs(name: String)
  case class ActionFlowJs(action: ActionJs, activity: Option[ActivityJs])
  case class ContributionJs(
    participantId: UUID,
    policyList: List[UUID] = List.empty,
    payloadAuthCode: String = "*",
    contributeTypeList: List[String] = List.empty,
    accessibleActionList: List[ActionJs] = List.empty
  )
  case class ActivityFlowJs(
    activityJs: ActivityJs,
    contribution: List[ContributionJs],
    actionFlow: List[ActionFlowJs]
  )
  case class WorkflowTemplateJs(
    id: UUID,
    name: String,
    startActivity: ActivityJs,
    activityFlowList: Seq[ActivityFlowJs]
  )

}
