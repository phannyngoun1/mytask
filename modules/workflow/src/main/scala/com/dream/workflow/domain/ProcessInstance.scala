package com.dream.workflow.domain

import java.util.UUID

import com.dream.common.domain.ErrorMessage


object ProcessInstance {

  sealed trait InstError extends ErrorMessage

  case class DefaultInstError(message: String) extends InstError

  case class InvalidInstStateError(override val id: Option[UUID] = None) extends InstError {

    override val message: String = s"Invalid state${id.fold("")(id => s":id = ${id.toString}")}"
  }

//
//  case class AssignedTask(
//    id: UUID,
//    description: String,
//    participants: List[UUID],
//    //Priority
//    activity: BaseActivity
//  )

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

case class ProcessInstance(
  id: UUID,
  createdBy: UUID,
  flowId: UUID,
  folio: String,
  contentType: String,
  description: String,
  tasks: List[Task],
  isActive: Boolean = true
)