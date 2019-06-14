package com.dream.mytask.shared.data

import java.util.UUID

case class TaskItemJson(
  id: String,
  pInstId: String,
  participantId: String,
  activityName: String,
  actions: List[ActionItemJson]
)


case class TaskInfoJs(
  id: UUID,
  activity: String,
//  actions: List[ActionItemJson],
  destinations: List[TaskDestinationJs],
  actionPerformed: List[ActionInfoJs] = List.empty,
  dateCreated: Long ,
  active: Boolean
)

case class ActionInfoJs(
  id: UUID,
  participantId: UUID,
  action: ActionItemJson,
  actionDate: Long,
  comment: Option[String]
)

case class TaskDestinationJs(
  participantId: UUID, active: Boolean
)


case class ActionItemJson(
  name: String,
  payloadCode: Option[String]
)

case class ActionInfoJson(
  activity: String,
  action: String,
  taskId: UUID,
  pInstId: UUID,
  accountId: UUID,
  participantId: UUID

)