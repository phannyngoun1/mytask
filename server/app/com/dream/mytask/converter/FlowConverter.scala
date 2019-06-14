package com.dream.mytask.converter

import com.dream.common.{Action, ActionFlow, Activity, ActivityFlow, BaseActivityFlow, Contribution}
import com.dream.mytask.shared.data.WorkflowData.{ActionFlowJs, ActionJs, ActivityFlowJs, ActivityJs, ContributeTypeJs, ContributionJs}

import scala.language.implicitConversions

object FlowConverter {

  implicit def toContribution(ori: ContributionJs) =
    Contribution(
      participantId = ori.participantId,
      policyList = ori.policyList,
      payloadAuthCode = ori.payloadAuthCode,
      contributeTypeList = ori.contributeTypeList,
      accessibleActionList = ori.accessibleActionList.map(act => Action(act.name, act.actionType))
    )

  implicit def toContributionJs(ori: Contribution) = {
    ContributionJs(
      participantId = ori.participantId,
      policyList = ori.policyList,
      payloadAuthCode = ori.payloadAuthCode,
      contributeTypeList = ori.contributeTypeList,
      accessibleActionList = ori.accessibleActionList.map(act => ActionJs(act.name, act.actionType))
    )
  }

  implicit def toActionFlow(ori: ActionFlowJs) =
    ActionFlow(
      action = Action(ori.action.name, ori.action.actionType),
      payloadCode = ori.payloadCode,
      activity = ori.activity.map(act => Activity(act.name))
    )

  implicit def toActionFlowJs(ori: ActionFlow) =
    ActionFlowJs(
      action = ActionJs(ori.action.name, ori.action.actionType),
      payloadCode = ori.payloadCode,
      activity = ori.activity.map(act =>  ActivityJs(act.name) )
    )

  implicit def toActivityFlow(ori: ActivityFlowJs) =
    ActivityFlow(
      activity = Activity(ori.activityJs.name),
      contributeTypeList = ori.contributeTypes.map(_.code),
      contribution = ori.contribution.map(toContribution),
      actionFlows = ori.actionFlow.map(toActionFlow)
    )

  implicit def toActivityFlowJs(ori: BaseActivityFlow) =
    ActivityFlowJs(
      activityJs = ActivityJs(ori.activity.name),
      contributeTypes = ori.contributeTypeList.map(ContributeTypeJs.find ),
      contribution = ori.contribution.map(toContributionJs),
      actionFlow = ori.actionFlows.map(toActionFlowJs)
    )
}
