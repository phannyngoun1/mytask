package com.dream.workflow.adaptor.journal

import akka.persistence.journal.{Tagged, WriteEventAdapter}

class WorkflowAdaptor extends WriteEventAdapter {

  private def withTag(event: Any, tag: String) = Tagged(event, Set(tag))
  private val tagName = "workflow"

  override def manifest(event: Any): String = ""

  override def toJournal(event: Any): Any = withTag(event, tagName)
}
