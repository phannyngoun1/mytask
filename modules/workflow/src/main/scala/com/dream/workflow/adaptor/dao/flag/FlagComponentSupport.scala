package com.dream.workflow.adaptor.dao.flag

trait  FlagComponentSupport {  this: FlagComponent =>
  trait FlagDaoSupport { this: DaoSupport[String, FlagRecord] =>


  }
}
