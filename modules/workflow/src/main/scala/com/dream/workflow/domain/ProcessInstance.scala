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
    actionPerformedId: UUID,
    taskId: UUID,
    participantId: UUID,
    action: BaseAction,
    processAt: Instant,
    comment: Option[String]
  ) extends ProcessInstanceEvent

  case class TaskReassigned(
    taskId: UUID,
    newParticipantId: UUID
  ) extends ProcessInstanceEvent

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

  def commitTask(actionPerformedId: UUID,taskId: UUID, participantId: UUID, action: BaseAction, actionDate: Instant, comment: Option[String]): Either[InstError, ProcessInstance] = {
    Right(
      copy(tasks = tasks.map(task =>
        if(task.id.equals(taskId))
          task.copy(
            active = !action.actionType.equals("COMPLETED"),
            destinations = task.destinations.map { dest =>
              if (dest.participantId.equals(participantId)) dest.copy(isActive = action.actionType.equals("HANDLING")) else dest
            },
            actionPerformed = ActionPerformed(actionPerformedId, participantId, action, actionDate, comment ) :: task.actionPerformed
          )
        else
          task
      ))
    )
  }

  def reRoute(taskId: UUID, newParticipantId: UUID) : Either[InstError, ProcessInstance] =
    Right(copy(
      tasks = tasks.map(task =>
        if(task.id.equals(taskId))
          task.copy(
            destinations = TaskDestination(newParticipantId) :: task.destinations
          )
        else
          task
      )
    ))


}