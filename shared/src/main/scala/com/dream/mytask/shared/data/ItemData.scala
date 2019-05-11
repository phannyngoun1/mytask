package com.dream.mytask.shared.data

import com.dream.mytask.shared.data.WorkflowData.FlowJson

object ItemData {

  case class ItemJson (
    id: String,
    name: String
  )

  case class ItemInitDataJs(
    list: List[ItemJson],
    flowList: List[FlowJson]
  )

}
