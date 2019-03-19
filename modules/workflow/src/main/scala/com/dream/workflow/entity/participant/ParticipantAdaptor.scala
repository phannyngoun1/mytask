package com.dream.workflow.entity.participant

import akka.persistence.journal.{Tagged, WriteEventAdapter}
import com.dream.workflow.domain.Account.AccountEvent
import com.dream.workflow.domain.Participant.ParticipantEvent

class ParticipantAdaptor  extends WriteEventAdapter {

  private def withTag(event: Any, tag: String) = Tagged(event, Set(tag))
  private val tagName = classOf[ParticipantEvent].getName

  override def manifest(event: Any): String = ""

  override def toJournal(event: Any): Any = withTag(event, tagName)
}
