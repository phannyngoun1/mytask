package com.dream.workflow.adaptor.dao.flag

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Source}
import com.dream.workflow.usecase.port.FlagReadModelFlows
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext

class FlagReadModelFlowImpl(val profile: JdbcProfile, val db: JdbcProfile#Backend#Database)
  extends FlagComponent with FlagReadModelFlows {

  import profile.api._

  override def update(implicit ec: ExecutionContext): Flow[(String, Long), Int, NotUsed] =
    Flow[(String, Long)].mapAsync(1) {
      case (code, lastNr) =>
        db.run(
          FlagDao.filter(_.code === code)
            .map(e=> (e.sequenceNr))
            .update(lastNr)
        )
    }

  override def resolveLastSeqNrSource(implicit ec: ExecutionContext): Source[Long, NotUsed] =
    Source.single(1).mapAsync(1) { _ =>
      db.run(
        FlagDao.filter(_.code === "workflow")
          .map(_.sequenceNr).max.result
      ).map(_.getOrElse(0))
    }
}
