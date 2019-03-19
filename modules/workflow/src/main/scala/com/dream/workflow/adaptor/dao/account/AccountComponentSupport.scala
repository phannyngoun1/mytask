package com.dream.workflow.adaptor.dao.account

trait AccountComponentSupport { this: AccountComponent =>

  trait AccountDaoSupport { this: DaoSupport[String, AccountRecord] =>

  }

}
