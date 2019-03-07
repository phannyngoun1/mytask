package com.dream.workflow.entity.item

import akka.persistence.journal.{Tagged, WriteEventAdapter}
import com.dream.workflow.domain.ItemEvent

class ItemEventAdaptor extends WriteEventAdapter {

  private def withTag(event: Any, tag: String) = Tagged(event, Set(tag))

  private val tagName = classOf[ItemEvent].getName

  override def manifest(event: Any): String  = ""

  override def toJournal(event: Any): Any = {
    withTag(event, tagName)
  }
}
