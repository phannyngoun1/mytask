package com.dream.mytask.shared.data

case class TaskItemJson(
  id: String,
  pInstId: String,
  activityName: String,
  actions: List[ActionItemJson]
)

case class ActionItemJson(
  name: String
)

