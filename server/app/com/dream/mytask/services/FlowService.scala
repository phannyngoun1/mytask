package com.dream.mytask.services

import java.util.UUID

import com.dream.common._
import com.dream.mytask.shared.data.AccountData.ParticipantJson
import com.dream.mytask.shared.data.WorkflowData
import com.dream.mytask.shared.data.WorkflowData.{ActionFlowJs, ActionJs, ActivityFlowJs, ActivityJs, ContributeTypeJs, FlowInitDataJs, FlowJson, WorkflowTemplateJs}
import com.dream.ticket.flow.TicketFlowTemplate
import com.dream.workflow.usecase.WorkflowAggregateUseCase.Protocol.{CreateWorkflowCmdRequest, CreateWorkflowCmdSuccess, GetWorkflowCmdRequest, GetWorkflowCmdSuccess}
import com.dream.mytask.converter.FlowConverter._

import scala.concurrent.Future

trait FlowService {  this: ApiService =>



  val  ticketWorkflowTemplate = TicketFlowTemplate.apply()
  val flowTemplateList = List(
    WorkflowTemplateJs(
      id = UUID.randomUUID(),
      name = ticketWorkflowTemplate.name,
      startActivity = ActivityJs(ticketWorkflowTemplate.startActivity.name),
      activityFlowList = ticketWorkflowTemplate.activityFlowList.map(item =>
        ActivityFlowJs(
          ActivityJs(item.activity.name),
          contributeTypes = item.contributeTypeList.map(ct => ContributeTypeJs.find(ct)),
          contribution = List.empty,
          actionFlow = item.actionFlows.map(actFlow =>
            ActionFlowJs(ActionJs( actFlow.action.name, actFlow.action.actionType), actFlow.payloadCode, actFlow.activity.map(activity => ActivityJs(activity.name)))
          )
        )
      )
    )
  )

  override def getFlowInitData(): Future[FlowInitDataJs] = {
    val data = for {
      list <- getFlowList()
      pcpList <- getParticipantList()
    } yield (list, pcpList)

    data.map { item =>
      FlowInitDataJs(
        item._1.map(flow => FlowJson(flow.id, flow.name)),
        flowTemplateList,
        item._2.map(pcp => ParticipantJson(pcp.id, pcp.accountId, pcp.tasks) )
      )
    }
  }

  override def getFlowTemplate(id: UUID): Future[WorkflowData.WorkflowTemplateJs] = {
    getFlowInitData().map{ item =>
      flowTemplateList.find(_.id.equals(id)).map(_.copy(flowInitDataJs = Some(item))) .get
    }
  }

  override def getFlow(id: UUID): Future[WorkflowTemplateJs] =
    workflowAggregateUseCase.getWorkflow(GetWorkflowCmdRequest(id)) map {
      case GetWorkflowCmdSuccess(flow) =>
        WorkflowTemplateJs(
          id = flow.id,
          name = flow.name,
          startActivity = ActivityJs(flow.initialActivity.name),
          activityFlowList = flow.workflowList.map(toActivityFlowJs)
        )
      case _ => throw new RuntimeException(s"Error: getFlow ${id}")
    }

  override def newFlow(workflow: WorkflowTemplateJs): Future[String] = {

    val startAct = Activity(workflow.startActivity.name)
    val actList =  workflow.activityFlowList.map(item => ActivityFlow(
      activity = Activity(item.activityJs.name),
      contributeTypeList = List.empty,
      contribution = item.contribution.map(toContribution),
      actionFlows = item.actionFlow.map(toActionFlow)
    ))

    workflowAggregateUseCase.createWorkflow(CreateWorkflowCmdRequest(
      UUID.randomUUID(),
      workflow.name,
      startAct,
      actList
    )).map {
      case res: CreateWorkflowCmdSuccess => s"${res.id}"
      case _ => "Failed"
    }
  }

  override def getFlowList(): Future[List[WorkflowData.FlowJson]] =
    workflowAggregateUseCase.list.map(_.map(item => FlowJson(item.id.toString, item.name)))

}
