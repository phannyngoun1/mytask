package com.dream.workflow.domain

import java.util.UUID

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
  def takAction(task: Task, by: UUID):  Either[InstError, ProcessInstance] = {
    val state = copy(tasks = tasks.map(item => if (item.active) item.copy(active = false) else item ))
    Right(state.copy(tasks = task :: tasks))
  }

}