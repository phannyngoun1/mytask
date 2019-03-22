package com.dream.workflow.adaptor.dao.processinstance

trait PInstanceComponentSupport { this: PInstanceComponent =>

  trait PInstanceDaoSupport {  this: DaoSupport[String, PInstanceRecord] =>

  }
}
