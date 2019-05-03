package com.dream.mytask.shared.data


object WorkflowData {

  case class FlowJson(id: String, name: String)

  sealed trait PayloadJs

  case class EditTicketPayloadJs(test: String) extends PayloadJs

}
