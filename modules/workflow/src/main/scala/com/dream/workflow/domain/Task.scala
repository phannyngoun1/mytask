package com.dream.workflow.domain

import java.util.UUID
import java.time.Instant

import com.dream.common.{BaseAction, BaseActivity}

case class TaskDto(
  id: UUID,
  pInstId: UUID,
  participantId: UUID,
  activity: BaseActivity,
  actions: List[BaseAction],
  active: Boolean,
  isOwner: Boolean = true
)

case class Task(
  id: UUID,
  activity: BaseActivity,
  actions: List[BaseAction],
  destinations: List[TaskDestination],
  actionPerformed: List[ActionPerformed] = List.empty,
  dateCreated: Instant = Instant.now(),
  active: Boolean = true
)

case class ActionPerformed(
  id: UUID,
  participantId: UUID,
  action: BaseAction,
  actionDate: Instant,
  comment: Option[String]
)

case class TaskDestination(participantId: UUID, dateCreated: Instant = Instant.now() , isActive: Boolean = true)

case class AssignedTask(
  taskId: UUID,
  pInstId: UUID
)

trait TaskStatus

case object CompletedTaskStatus extends TaskStatus
case object HandledTaskStatus extends TaskStatus
case object HandlingTaskStatus extends TaskStatus