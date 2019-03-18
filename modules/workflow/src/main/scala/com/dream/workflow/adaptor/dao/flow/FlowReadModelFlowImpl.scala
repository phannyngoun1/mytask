package com.dream.workflow.adaptor.dao.flow

import java.time.Instant
import java.util.UUID

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Source}
import com.dream.workflow.domain.FlowDto
import com.dream.workflow.usecase.port.FlowReadModelFlow
import org.sisioh.baseunits.scala.time.TimePoint
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class FlowReadModelFlowImpl(val profile: JdbcProfile, val db: JdbcProfile#Backend#Database)
  extends FlowComponent
    with FlowReadModelFlow {

  import profile.api._

  override def resolveLastSeqNrSource(implicit ec: ExecutionContext): Source[Long, NotUsed] =
    Source.single(1).mapAsync(1) { _ =>
      db.run(FlowDao.map(_.sequenceNr).max.result)
        .map(_.getOrElse(0L))
    }

  override def list = {
    db.stream(FlowDao.sortBy(_.createdAt).result).mapResult(item => FlowDto(UUID.fromString(item.id), item.name, TimePoint.from(item.createdAt), TimePoint.from(item.updatedAt), item.active))
  }

  override def newItemFlow(implicit ec: ExecutionContext): Flow[(UUID, String, Long, TimePoint), Int, NotUsed] = {
    Flow[(UUID, String, Long, TimePoint)].mapAsync(1) {
      case (id, name, seq, createdAt) =>
        db.run(
          FlowDao.forceInsert(
            FlowRecord(id.toString, name, seq, createdAt.asJavaZonedDateTime(), createdAt.asJavaZonedDateTime(), true)
          )
        )
    }
  }
}
