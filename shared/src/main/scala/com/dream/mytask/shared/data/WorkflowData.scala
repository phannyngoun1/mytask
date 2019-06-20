package com.dream.mytask.shared.data

import java.util.UUID

import com.dream.mytask.shared.data.AccountData.ParticipantJson


object WorkflowData {

  case class FlowJson(id: String, name: String)

  sealed trait PayloadJs {
    def payloadCode: Option[String]
    def comment: Option[String]
  }

  case class EditTicketPayloadJs( payloadCode: Option[String], test: String, override val comment: Option[String]) extends PayloadJs

  case class TicketStatusPayloadJs(payloadCode: Option[String], status: String,  override val comment: Option[String]) extends PayloadJs

  case class AssignTicketPayloadJs(payloadCode: Option[String], participantId: UUID, override val comment: Option[String]) extends PayloadJs

  case class CommentPayloadJs(payloadCode: Option[String], override val comment: Option[String]) extends PayloadJs

  case class FlowInitDataJs(
    list: List[FlowJson],
    workflowTemplateList: List[WorkflowTemplateJs],
    //contributeTypeList: List[ContributeTypeJs],
    pcpList: List[ParticipantJson]
  )

  case class ActionJs(name: String, actionType: String)
  case class ActivityJs(name: String)
  case class ActionFlowJs(action: ActionJs, payloadCode: Option[String] , activity: Option[ActivityJs])

  case class ContributeTypeJs(code: String, name: String)


  object ContributeTypeJs {

    def find(code: String) =
      code match {
        case "DirectAssign"  => directAssign
        case "Sharable" => sharable
        case "Assignable" => assignable
        case "Pickup" => pickup
        case "*" => all
      }

    val directAssign = ContributeTypeJs("DirectAssign", "Direct assign")
    val sharable = ContributeTypeJs("Sharable", "Can be shared")
    val assignable = ContributeTypeJs("Assignable", "Can be assigned")
    val pickup = ContributeTypeJs("Pickup", "Pickup")
    val all = ContributeTypeJs("*", "*")
  }

  case class ContributionJs(
    participantId: UUID,
    policyList: List[UUID] = List.empty,
    payloadAuthCode: String = "*",
    contributeTypeList: List[String] = List.empty,
    accessibleActionList: List[ActionJs] = List.empty
  )

  case class ActivityFlowJs(
    activityJs: ActivityJs,
    contributeTypes: List[ContributeTypeJs],
    contribution: List[ContributionJs],
    actionFlow: List[ActionFlowJs]
  )

  case class WorkflowTemplateJs(
    id: UUID,
    name: String,
    startActivity: ActivityJs,
    activityFlowList: Seq[ActivityFlowJs],
    flowInitDataJs: Option[FlowInitDataJs] = None
  )



}
