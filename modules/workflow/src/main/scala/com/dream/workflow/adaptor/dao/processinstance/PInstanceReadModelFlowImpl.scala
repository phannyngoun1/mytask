package com.dream.workflow.adaptor.dao.processinstance

import java.util.UUID

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Source}
import com.dream.workflow.domain.ProcessInstanceDto
import com.dream.workflow.usecase.port.PInstanceReadModelFlows
import org.sisioh.baseunits.scala.time.TimePoint
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext

class PInstanceReadModelFlowImpl(val profile: JdbcProfile, val db: JdbcProfile#Backend#Database) extends PInstanceComponent
  with PInstanceReadModelFlows {

  import profile.api._

  override def resolveLastSeqNrSource(implicit ec: ExecutionContext): Source[Long, NotUsed] =
    Source.single(1).mapAsync(1) { _ =>
      db.run(PInstanceDao.map(_.sequenceNr).max.result)
        .map(_.getOrElse(0L))
    }

  override def newPInst(implicit ec: ExecutionContext): Flow[(UUID, String, Long, TimePoint), Int, NotUsed] =
    Flow[(UUID, String, Long, TimePoint)].mapAsync(1) {
      case (id, folio, seq, createdAt) =>
        db.run(
          PInstanceDao.forceInsert(
            PInstanceRecord(id.toString, folio, seq, createdAt.asJavaZonedDateTime(), createdAt.asJavaZonedDateTime())
          )
        )
    }

  override def list =
    db.stream(PInstanceDao.sortBy(_.createdAt).result).mapResult(item => ProcessInstanceDto(UUID.fromString(item.id), item.folio))
}
