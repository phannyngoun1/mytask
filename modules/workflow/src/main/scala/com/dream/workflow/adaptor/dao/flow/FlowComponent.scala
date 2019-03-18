package com.dream.workflow.adaptor.dao.flow

import com.dream.workflow.adaptor.dao.ComponentSupport

trait FlowComponent extends ComponentSupport  with FlowComponentSupport{

  import profile.api._

  case class FlowRecord(
    id: String,
    name: String,
    sequenceNr: Long,
    createdAt: java.time.ZonedDateTime,
    updatedAt: java.time.ZonedDateTime,
    active: Boolean
  ) extends Record

  case class Items(tag: Tag) extends TableBase[FlowRecord](tag, "workflow") {

    def name       = column[String]("name")
    def sequenceNr = column[Long]("sequence_nr")
    def createdAt  = column[java.time.ZonedDateTime]("created_at")
    def updatedAt  = column[java.time.ZonedDateTime]("updated_at")
    def active = column[Boolean]("active")

    override def * =
      (id, name, sequenceNr, createdAt, updatedAt, active) <> (FlowRecord.tupled, FlowRecord.unapply)
  }

  object FlowDao
    extends TableQuery(Items)
      with DaoSupport[String, FlowRecord]
      with FlowDaoSupport

}
