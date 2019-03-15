package com.dream.mytask.services

import java.util.UUID

import com.dream.mytask.shared.data.WorkflowData
import com.dream.mytask.shared.data.WorkflowData.FlowJson
import com.dream.workflow.usecase.WorkflowAggregateUseCase.Protocol.{GetWorkflowCmdRequest, GetWorkflowCmdSuccess}

import scala.concurrent.Future

trait FlowService {  this: ApiService =>

  override def getFlow(id: String): Future[WorkflowData.FlowJson] =
    workflowAggregateUseCase.getWorkflow(GetWorkflowCmdRequest(UUID.fromString(id))) map {
      case GetWorkflowCmdSuccess(flow) => FlowJson(flow.id.toString, flow.name)
      case _ => FlowJson("", "")
    }

  override def newFlow(name: String): Future[String] = ???

}
