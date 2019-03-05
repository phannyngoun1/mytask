package com.dream.workflow.entity.processinstance

import java.util.UUID

import com.dream.common.Protocol.{CmdRequest, CmdResponse}
import com.dream.workflow.domain._

object ProcessInstanceProtocol {

  sealed trait ProcessInstanceCmdRequest extends CmdRequest {
    def id: UUID
  }

  sealed trait TaskCmdRequest extends CmdRequest

  sealed trait ProcessInstanceCmdResponse extends CmdResponse

  sealed trait TaskCmdResponse extends CmdResponse

  trait CreatePInstCmdResponse extends ProcessInstanceCmdResponse

  abstract class PerformTaskCmdRes() extends TaskCmdResponse

  case class CreatePInstCmdRequest(
    id: UUID,
    createdBy: UUID,
    flowId: UUID,
    folio: String,
    contentType: String,
    description: String,
    destIds: List[UUID],
    task: Task
  ) extends ProcessInstanceCmdRequest

  case class CreatePInstCmdSuccess(id: UUID) extends CreatePInstCmdResponse

  case class GetPInstCmdRequest(
    id: UUID
  ) extends ProcessInstanceCmdRequest

  case class GetPInstCmdSuccess(
    processInstance: ProcessInstance
  ) extends ProcessInstanceCmdResponse

  case class PerformTaskCmdReq(
    pInstId: UUID,
    activity: BaseActivity,
  ) extends TaskCmdRequest

  case class PerformTaskSuccess() extends PerformTaskCmdRes

  case class GetTaskCmdReq(
    id: UUID,
    taskId: UUID
  ) extends ProcessInstanceCmdRequest

  case class GetTaskCmdRes(task: Task) extends PerformTaskCmdRes


}
