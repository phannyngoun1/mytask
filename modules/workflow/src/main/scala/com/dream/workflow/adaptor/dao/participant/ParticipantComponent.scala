package com.dream.workflow.adaptor.dao.participant

import com.dream.common.dao.ComponentSupport

trait ParticipantComponent  extends ComponentSupport with ParticipantComponentSupport {

  import profile.api._

  case class ParticipantRecord(
    id: String,
    accountId: String,
    teamId: String,
    departmentId: String,
    propertyId: String,
    sequenceNr: Long,
    createdAt: java.time.ZonedDateTime,
    updatedAt: java.time.ZonedDateTime,
    active: Boolean
  ) extends Record

  case class Participants(tag: Tag) extends TableBase[ParticipantRecord](tag, "participant") {
    // def id = column[Long]("id", O.PrimaryKey)
    def name          = column[String]("name")
    def accountId     = column[String]("account_id")
    def teamId        = column[String]("team_id")
    def departmentId      = column[String]("department_id")
    def propertyId        = column[String]("property_id")
    def sequenceNr        = column[Long]("sequence_nr")
    def createdAt  = column[java.time.ZonedDateTime]("created_at")
    def updatedAt  = column[java.time.ZonedDateTime]("updated_at")
    def active = column[Boolean]("active")
    override def * =
      (id, accountId, teamId, departmentId, propertyId, sequenceNr, createdAt, updatedAt, active) <> (ParticipantRecord.tupled, ParticipantRecord.unapply)
  }

  object ParticipantDao
    extends TableQuery(Participants)
      with DaoSupport[String, ParticipantRecord]
      with ParticipantDaoSupport

}
