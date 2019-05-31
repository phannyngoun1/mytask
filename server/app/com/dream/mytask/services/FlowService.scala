package com.dream.mytask.services

import java.util.UUID

import com.dream.common._
import com.dream.mytask.shared.data.AccountData.ParticipantJson
import com.dream.mytask.shared.data.WorkflowData
import com.dream.mytask.shared.data.WorkflowData.{ActionFlowJs, ActionJs, ActivityFlowJs, ActivityJs, FlowInitDataJs, FlowJson, WorkflowTemplateJs}
import com.dream.ticket.flow.TicketFlowTemplate
import com.dream.workflow.usecase.WorkflowAggregateUseCase.Protocol.{CreateWorkflowCmdRequest, CreateWorkflowCmdSuccess, GetWorkflowCmdRequest, GetWorkflowCmdSuccess}

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
          contribution = List.empty,
          actionFlow = item.actionFlows.map(actFlow =>
            ActionFlowJs(ActionJs( actFlow.action.name, actFlow.action.actionType), actFlow.activity.map(activity => ActivityJs(activity.name)))
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
    Future.successful(flowTemplateList.find(_.id.equals(id)).get)
  }

  override def getFlow(id: String): Future[WorkflowData.FlowJson] =
    workflowAggregateUseCase.getWorkflow(GetWorkflowCmdRequest(UUID.fromString(id))) map {
      case GetWorkflowCmdSuccess(flow) => FlowJson(flow.id.toString, flow.name)
      case _ => FlowJson("", "")
    }

  override def newFlow(name: String, participants: List[String]): Future[String] = {

    val ticketActivity = Activity("Ticketing")

    val startActionFlow = ActionFlow(
      action = StartAction(),
      activity = Some(ticketActivity)
    )

    val startActivityFlow = ActivityFlow(
      activity = StartActivity(),
      participants = List.empty,
      actionFlows = List(startActionFlow)
    )


    val editTicketActionFlow = ActionFlow(
      action = Action("Edit", "HANDLING"),
      None
    )

    val closeTicketActionFlow = ActionFlow(
      action = Action("Close", "COMPLETED"),
      activity = Some(DoneActivity())
    )

    val assignTicketActionFlow = ActionFlow(
      action = Action("Assign", "HANDLED"),
      activity = None
    )

    val addCommentActionFlow = ActionFlow(
      action = Action("Comment", "HANDLING"),
      activity = None
    )

    val ticketActivityFlow = ActivityFlow(
      activity = ticketActivity,
      participants = participants.map(UUID.fromString),

      actionFlows = List(
        editTicketActionFlow,
        closeTicketActionFlow,
        assignTicketActionFlow,
        addCommentActionFlow
      )
    )

    val workflowList: Seq[BaseActivityFlow] = Seq(
      startActivityFlow,
      ticketActivityFlow
    )

    workflowAggregateUseCase.createWorkflow(CreateWorkflowCmdRequest(UUID.randomUUID(), name, StartActivity(), workflowList)).map {
      case res: CreateWorkflowCmdSuccess => s"${res.id}"
      case _ => "Failed"
    }
  }

  override def getFlowList(): Future[List[WorkflowData.FlowJson]] =
    workflowAggregateUseCase.list.map(_.map(item => FlowJson(item.id.toString, item.name)))

}
