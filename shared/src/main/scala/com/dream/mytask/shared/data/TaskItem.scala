package com.dream.mytask.shared.data

import java.util.UUID

case class TaskItem(
  id: UUID,
  activityName: String,
  actions: List[ActionItem]
)

case class ActionItem(
  name: String
)

