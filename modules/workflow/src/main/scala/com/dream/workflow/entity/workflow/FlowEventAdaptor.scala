package com.dream.workflow.entity.workflow

import akka.persistence.journal.{Tagged, WriteEventAdapter}
import com.dream.workflow.domain.FlowEvents.FlowEvent

class FlowEventAdaptor extends WriteEventAdapter {

  private def withTag(event: Any, tag: String) = Tagged(event, Set(tag))
  private val tagName = classOf[FlowEvent].getName

  override def manifest(event: Any): String = ""

  override def toJournal(event: Any): Any = withTag(event, tagName)
}
