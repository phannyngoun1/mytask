package com.dream.workflow.domain

import java.util.UUID
import java.time.Instant

case class Task(
  id: UUID,
  destIds: List[UUID],
  activityName: BaseActivity,
  actions: List[BaseAction],
  dateCreated: Instant = Instant.now(),

  performBy: Option[UUID] = None,
  datePerformed: Option[Instant] = None
)


case class AssignedTask(
  taskId: UUID,
  pInstId: UUID
)