package com.dream.workflow.domain

import java.time.Instant
import java.util.UUID

import com.dream.common.BaseAction
import com.dream.common.domain.ErrorMessage
import com.dream.workflow.domain.ProcessInstance.InstError


object ProcessInstance {

  sealed trait InstError extends ErrorMessage

  case class DefaultInstError(message: String) extends InstError

  case class InvalidInstStateError(override val id: Option[UUID] = None) extends InstError {

    override val message: String = s"Invalid state${id.fold("")(id => s":id = ${id.toString}")}"
  }

  case class InstanceType(
    id: UUID,
    name: String,
    description: String
  )


  sealed trait ProcessInstanceEvent

  case class ProcessInstanceCreated(

    id: UUID,
    createdBy: UUID,
    flowId: UUID,
    folio: String,
    contentType: String,
    description: String,
    task: Task,
  ) extends ProcessInstanceEvent

  case class NewTaskCreated(
    id: UUID,
    task: Task,
    participantId: UUID
  ) extends ProcessInstanceEvent

  case class ActionCommitted(
    id: UUID,
    taskId: UUID,
    participantId: UUID,
    action: BaseAction,
    processAt: Instant
  )

}


case class ProcessInstanceDto(
  id: UUID,
  folio: String
)

case class ProcessInstance(
  id: UUID,
  createdBy: UUID,
  flowId: UUID,
  folio: String,
  contentType: String,
  description: String,
  tasks: List[Task],
  isActive: Boolean = true
) {
  def createTask(task: Task, by: UUID): Either[InstError, ProcessInstance] = {
    Right(copy(tasks = task :: tasks))
  }

  def commitTask(taskId: UUID, participantId: UUID, action: BaseAction, actionDate: Instant): Either[InstError, ProcessInstance] = {
    Right(
      copy(tasks = tasks.map(task => task.copy(
        destinations = task.destinations.map { dest =>
          if (dest.participantId.equals(participantId)) dest.copy(action = Some(action), actionDate = Some(actionDate)) else dest
        },
        active = false
      )))
    )
  }

}