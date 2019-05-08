package com.dream.mytask.shared.data

import java.util.UUID

case class TaskItemJson(
  id: String,
  pInstId: String,
  participantId: String,
  activityName: String,
  actions: List[ActionItemJson]
)

case class ActionItemJson(
  name: String
)

case class ActionInfoJson(
  activity: String,
  action: String,
  taskId: UUID,
  pInstId: UUID,
  accountId: UUID,
  participantId: UUID

)