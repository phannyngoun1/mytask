package com.dream.workflow.adaptor.dao.account

import com.dream.workflow.adaptor.dao.ComponentSupport

trait AccountComponent  extends ComponentSupport with AccountComponentSupport {

  import profile.api._

  case class AccountRecord(
    id: String,
    name: String,
    fullName: String,
    sequenceNr: Long,
    createdAt: java.time.ZonedDateTime,
    updatedAt: java.time.ZonedDateTime,
    active: Boolean
  ) extends Record

  case class Accounts(tag: Tag) extends TableBase[AccountRecord](tag, "account") {
    // def id = column[Long]("id", O.PrimaryKey)
    def name          = column[String]("name")
    def fullName     = column[String]("full_name")
    def sequenceNr        = column[Long]("sequence_nr")
    def createdAt  = column[java.time.ZonedDateTime]("created_at")
    def updatedAt  = column[java.time.ZonedDateTime]("updated_at")
    def active = column[Boolean]("active")
    override def * =
      (id, name, fullName, sequenceNr, createdAt, updatedAt, active) <> (AccountRecord.tupled, AccountRecord.unapply)
  }

  object AccountDao
    extends TableQuery(Accounts)
      with DaoSupport[String, AccountRecord]
      with AccountDaoSupport
}
