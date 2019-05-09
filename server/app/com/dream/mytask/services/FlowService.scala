package com.dream.mytask.services

import java.util.UUID

import com.dream.mytask.shared.data.WorkflowData
import com.dream.mytask.shared.data.WorkflowData.FlowJson
import com.dream.workflow.domain.{Action => FAction, _}
import com.dream.workflow.domain._
import com.dream.workflow.usecase.WorkflowAggregateUseCase.Protocol.{CreateWorkflowCmdRequest, CreateWorkflowCmdSuccess, GetWorkflowCmdRequest, GetWorkflowCmdSuccess}

import scala.concurrent.Future

trait FlowService {  this: ApiService =>

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
      action = FAction("Edit", "HANDLING"),
      None
    )

    val closeTicketActionFlow = ActionFlow(
      action = FAction("Close", "COMPLETED"),
      activity = Some(DoneActivity())
    )

    val assignTicketActionFlow = ActionFlow(
      action = FAction("Assign", "HANDLED"),
      activity = None
    )

    val addCommentActionFlow = ActionFlow(
      action = FAction("Comment", "HANDLING"),
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
