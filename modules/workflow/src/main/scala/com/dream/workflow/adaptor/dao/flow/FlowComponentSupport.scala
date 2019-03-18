package com.dream.workflow.adaptor.dao.flow

trait FlowComponentSupport {this: FlowComponent =>

  trait FlowDaoSupport { this: DaoSupport[String, FlowRecord] =>

  }

}
