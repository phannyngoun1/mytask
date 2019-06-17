package com.dream.workflow.entity.workflow

import java.util.UUID

import com.dream.common._
import com.dream.common.Protocol.{CmdRequest, CmdResponse}
import com.dream.workflow.domain.{Flow, Item, TaskDto}

object WorkflowProtocol {

  sealed trait WorkFlowCmdRequest extends CmdRequest {
    val id: UUID
  }

  sealed trait WorkFlowCmdResponse extends CmdResponse

  case class CreateWorkflowCmdRequest(
    id: UUID,
    name: String,
    initialActivity: BaseActivity,
    flowList: Seq[BaseActivityFlow]
  ) extends WorkFlowCmdRequest

  abstract class CreateWorkflowCmdResponse() extends WorkFlowCmdResponse

  case class CreateWorkflowCmdSuccess(id: UUID) extends CreateWorkflowCmdResponse

  case class GetWorkflowCmdRequest(
    id: UUID
  ) extends WorkFlowCmdRequest

  case class GetWorkflowCmdSuccess(
    workflow: Flow
  ) extends WorkFlowCmdResponse

  case class GetTaskActionCmdReq(id: UUID, task: TaskDto) extends WorkFlowCmdRequest

  case class GetTaskActionCmdSuccess(task: TaskDto) extends WorkFlowCmdResponse

  case class GetWorkflowPayloadCmdRequest(id: UUID, item: Item) extends WorkFlowCmdRequest

  case class GetWorkflowPayloadCmdSuccess(item: Item) extends WorkFlowCmdResponse


}
