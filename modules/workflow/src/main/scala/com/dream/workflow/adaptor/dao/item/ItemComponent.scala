package com.dream.workflow.adaptor.dao.item

import com.dream.workflow.adaptor.dao.ComponentSupport

trait ItemComponent extends ComponentSupport  with ItemComponentSupport {

  import profile.api._

  case class ItemRecord(
    id: String,
    name: String,
    sequenceNr: Long,
    createdAt: java.time.ZonedDateTime,
    updatedAt: java.time.ZonedDateTime
  ) extends Record

  case class Items(tag: Tag) extends TableBase[ItemRecord](tag, "item") {
    // def id = column[Long]("id", O.PrimaryKey)
    def name       = column[String]("name")
    def description           = column[String]("description")
    def sequenceNr = column[Long]("sequence_nr")
    def createdAt  = column[java.time.ZonedDateTime]("created_at")
    def updatedAt  = column[java.time.ZonedDateTime]("updated_at")
    override def * =
      (id, name, sequenceNr, createdAt, updatedAt) <> (ItemRecord.tupled, ItemRecord.unapply)
  }

  object ItemDao
    extends TableQuery(Items)
      with DaoSupport[String, ItemRecord]
      with ItemDaoSupport

}
