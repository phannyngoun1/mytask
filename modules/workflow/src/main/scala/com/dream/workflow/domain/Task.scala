package com.dream.workflow.domain

import java.util.UUID

case class Task(
  id: UUID,
  pInstId: UUID,
  folio: String,
  subject: String,
  participantId: UUID,
  activityName: String,
  action: List[BaseAction]
)
