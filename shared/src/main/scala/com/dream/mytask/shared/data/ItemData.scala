package com.dream.mytask.shared.data

import java.util.UUID

import com.dream.mytask.shared.data.WorkflowData.FlowJson

object ItemData {

  case class ItemJson (
    id: UUID,
    name: String,
    desc: Option[String],
    initPayloadCode: Option[String]

  )

  case class ItemInitDataJs(
    list: List[ItemJson],
    flowList: List[FlowJson]
  )

}
