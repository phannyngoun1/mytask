package com.dream.workflow.adaptor.dao.processinstance

import com.dream.common.dao.ComponentSupport

trait PInstanceComponent  extends ComponentSupport with PInstanceComponentSupport {

  import profile.api._

  case class PInstanceRecord(
    id: String,
    folio: String,
    sequenceNr: Long,
    createdAt: java.time.ZonedDateTime,
    updatedAt: java.time.ZonedDateTime
  ) extends Record

  case class PInstances(tag: Tag) extends TableBase[PInstanceRecord](tag, "process_instance") {
    // def id = column[Long]("id", O.PrimaryKey)
    def folio          = column[String]("folio")
    def sequenceNr        = column[Long]("sequence_nr")
    def createdAt  = column[java.time.ZonedDateTime]("created_at")
    def updatedAt  = column[java.time.ZonedDateTime]("updated_at")
    override def * =
      (id, folio, sequenceNr, createdAt, updatedAt ) <> (PInstanceRecord.tupled, PInstanceRecord.unapply)
  }

  object PInstanceDao
    extends TableQuery(PInstances)
      with DaoSupport[String, PInstanceRecord]
      with PInstanceDaoSupport
}
