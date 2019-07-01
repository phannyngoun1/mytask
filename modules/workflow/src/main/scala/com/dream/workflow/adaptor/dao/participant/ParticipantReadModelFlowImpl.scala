package com.dream.workflow.adaptor.dao.participant

import java.util.UUID

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Source}
import com.dream.common.dto.workflow.Account.ParticipantDto
import com.dream.workflow.usecase.port.ParticipantReadModelFlows
import org.sisioh.baseunits.scala.time.TimePoint
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext}

class ParticipantReadModelFlowImpl(val profile: JdbcProfile, val db: JdbcProfile#Backend#Database)
  extends ParticipantComponent with ParticipantReadModelFlows{

  import profile.api._

  override def resolveLastSeqNrSource(implicit ec: ExecutionContext): Source[Long, NotUsed] =
    Source.single(1).mapAsync(1) { _ =>
      db.run(ParticipantDao.map(_.sequenceNr).max.result)
        .map(_.getOrElse(0L))
    }

  override def newItemFlow(implicit ec: ExecutionContext): Flow[(UUID, UUID, UUID, UUID, UUID, Long, TimePoint), Int, NotUsed] =
    Flow[(UUID, UUID, UUID, UUID, UUID, Long, TimePoint)].mapAsync(1) {
      case (id, accountId, teamId, departmentId, propertyId, seq, createdAt) =>
        db.run(
          ParticipantDao.forceInsert(
            ParticipantRecord(
              id.toString,
              accountId.toString,
              teamId.toString,
              departmentId.toString,
              propertyId.toString, seq,
              createdAt.asJavaZonedDateTime(),
              createdAt.asJavaZonedDateTime(),
              true
            )
          )
        )
    }

  override def list =
    db.stream(ParticipantDao.sortBy(_.createdAt).result).mapResult(item => ParticipantDto(UUID.fromString(item.id), UUID.fromString(item.accountId)))

  def getParticipantByUser(id: UUID)(implicit ec: ExecutionContext) =
    db.run(
      ParticipantDao.filter(_.accountId === id.toString)
        .map(item => (item.id, item.accountId, item.active))
        .result.map(_.map(item => ParticipantDto(UUID.fromString(item._1), UUID.fromString(item._2))))
    )



}
