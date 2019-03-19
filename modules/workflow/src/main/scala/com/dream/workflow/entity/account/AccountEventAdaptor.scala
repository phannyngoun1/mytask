package com.dream.workflow.entity.account

import akka.persistence.journal.{Tagged, WriteEventAdapter}
import com.dream.workflow.domain.Account.AccountEvent

class AccountEventAdaptor  extends WriteEventAdapter {

  private def withTag(event: Any, tag: String) = Tagged(event, Set(tag))
  private val tagName = classOf[AccountEvent].getName

  override def manifest(event: Any): String = ""

  override def toJournal(event: Any): Any = withTag(event, tagName)
}
