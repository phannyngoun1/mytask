package com.dream.workflow.entity.processinstance

import akka.persistence.journal.{Tagged, WriteEventAdapter}
import com.dream.workflow.domain.ProcessInstance.ProcessInstanceEvent

class ProcessInstanceAdaptor extends WriteEventAdapter {

  private def withTag(event: Any, tag: String) = Tagged(event, Set(tag))
  private val tagName = classOf[ProcessInstanceEvent].getName

  override def manifest(event: Any): String = ""

  override def toJournal(event: Any): Any = withTag(event, tagName)
}
