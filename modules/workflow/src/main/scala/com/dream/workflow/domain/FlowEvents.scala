package com.dream.workflow.domain

import java.util.UUID

import com.dream.common.BaseActivity


object FlowEvents {

  sealed trait FlowEvent {
    val id: UUID
  }

  case class FlowCreated(
    override val id: UUID,
    name: String,
    initialActivity: BaseActivity,
    flowList: Seq[BaseActivityFlow],
  ) extends FlowEvent


//  object FlowCreated {
//    implicit val format: Format[FlowCreated] = Json.format
//  }

}
