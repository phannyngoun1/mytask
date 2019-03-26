package com.dream.workflow.domain

import java.util.UUID
import java.time.Instant

case class TaskDto(
  id: UUID,
  pInstId: UUID,
  activity: BaseActivity,
  actions: List[BaseAction]
)

case class Task(
  id: UUID,
  activity: BaseActivity,
  actions: List[BaseAction],
  destinations: List[TaskDestination],
  dateCreated: Instant = Instant.now(),
  active: Boolean = true
)

case class TaskDestination(participantId: UUID, action: Option[BaseAction] = None, actionDate: Option[Instant] = None)

case class AssignedTask(
  taskId: UUID,
  pInstId: UUID
)