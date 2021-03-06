package com.dream.common

import java.util.UUID

import com.dream.common.domain.ErrorMessage

object Protocol {

  trait CmdRequest

  trait CmdResponse

  trait TaskPerformCmdRequest {
    def taskId: UUID
    def action: BaseAction
    def activity: BaseActivity
    def payLoad: Payload
  }

  case class DefaultTaskPerformCmdRequest(
    taskId: UUID,
    action: BaseAction,
    activity: BaseActivity,
    payLoad: Payload
  ) extends TaskPerformCmdRequest

  trait TaskPerformCmdResponse {
    def activityId: UUID
  }

  case class DefaultTaskPerformCmdResponse(val activityId: UUID) extends TaskPerformCmdResponse

  case class CmdResponseFailed(errorMessage: ErrorMessage)

}
