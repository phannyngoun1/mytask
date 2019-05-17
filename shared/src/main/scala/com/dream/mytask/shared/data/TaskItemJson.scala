package com.dream.mytask.shared.data

import java.time.Instant
import java.util.UUID

import com.dream.mytask.shared.data.ProcessInstanceData.PInstInitDataInfoJs

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
  actions: List[ActionItemJson],
//  destinations: List[TaskDestination],
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