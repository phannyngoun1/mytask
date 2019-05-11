package com.dream.mytask.shared.data

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
}

case class CreateProcessIns(
  itemId: String,

)



