package com.dream.mytask.shared.data

import java.util.UUID

import com.dream.mytask.shared.data.AccountData.ParticipantJson
import com.dream.mytask.shared.data.ItemData.ItemJson


object ProcessInstanceData {

  case class PInstInitDataJson(
    list: List[ProcessInstanceJson],
    itemList: List[ItemJson],
    pcpList: List[ParticipantJson]
  )

  case class ProcessInstanceJson(
    id: String,
    folio: String
  )

  case class PInstInitDataInfoJs(
    pInstId: UUID,
    flowId: UUID,
    folio: String,
    contentType: String,
    description: String,
    active: Boolean,
    tasks: List[TaskInfoJs]
  )

}

case class CreateProcessIns(
  itemId: String,

)



