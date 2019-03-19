package com.dream.workflow.adaptor.dao.participant

trait ParticipantComponentSupport { this: ParticipantComponent =>

  trait ParticipantDaoSupport {
    this: DaoSupport[String, ParticipantRecord] =>

  }
}
