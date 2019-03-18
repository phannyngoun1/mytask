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

  override def newFlow(name: String): Future[String] = {

    val ticketActivity = Activity("Ticketing")

    val startActionFlow = ActionFlow(
      action = StartAction(),
      activity = ticketActivity
    )

    val startActivityFlow = ActivityFlow(
      activity = StartActivity(),
      participants = List.empty,
      actionFlows = List(startActionFlow)
    )


    val editTicketActionFlow = ActionFlow(
      action = FAction("Edit"),
      activity = CurrActivity()
    )

    val closeTicketActionFlow = ActionFlow(
      action = FAction("Close"),
      activity = DoneActivity()
    )

    val assignTicketActionFlow = ActionFlow(
      action = FAction("Assign"),
      activity = CurrActivity()
    )

    val addCommentActionFlow = ActionFlow(
      action = FAction("Comment"),
      activity = CurrActivity()
    )

    val ticketActivityFlow = ActivityFlow(
      activity = ticketActivity,
      participants = List(
        UUID.fromString("2329698-6019-4689-b41e-c21f2a5a0262"),
        UUID.fromString("c798826a-18ac-4935-9342-760d86296059")),
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

}
