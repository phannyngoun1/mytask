package com.dream.workflow.adaptor.dao.flag

import com.dream.workflow.adaptor.dao.ComponentSupport

trait FlagComponent  extends ComponentSupport with FlagComponentSupport {

  import profile.api._

  case class FlagRecord(
    id: String,
    code: String,
    value: Long
  ) extends Record

  case class Flags(tag: Tag) extends TableBase[FlagRecord](tag, "flag") {

    def code     = column[String]("code")
    def sequenceNr        = column[Long]("val")

    override def * =
      (id, code, sequenceNr ) <> (FlagRecord.tupled, FlagRecord.unapply)
  }

  object FlagDao
    extends TableQuery(Flags)
      with DaoSupport[String, FlagRecord]
      with FlagDaoSupport

}
