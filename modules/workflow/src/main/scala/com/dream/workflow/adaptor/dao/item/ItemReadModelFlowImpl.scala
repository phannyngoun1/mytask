package com.dream.workflow.adaptor.dao.item

import java.util.UUID

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Source}
import com.dream.workflow.domain.Item
import com.dream.workflow.usecase.port.ItemReadModelFlow
import org.sisioh.baseunits.scala.time.TimePoint
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext}

class ItemReadModelFlowImpl(val profile: JdbcProfile, val db: JdbcProfile#Backend#Database)
  extends ItemComponent
    with ItemReadModelFlow{

  import profile.api._

  override def resolveLastSeqNrSource(implicit ec: ExecutionContext): Source[Long, NotUsed] =
    Source.single(1).mapAsync(1) { _ =>
      db.run(ItemDao.map(_.sequenceNr).max.result)
        .map(_.getOrElse(0L))
    }


  override def newItemFlow(implicit ec: ExecutionContext): Flow[(UUID, String, Option[String], UUID,  Long, TimePoint), Int, NotUsed] = {
    Flow[(UUID, String , Option[String], UUID, Long, TimePoint)].mapAsync(1) {
      case (id, name, desc, workflowId , seq, createdAt) =>

        db.run(
          ItemDao.forceInsert(
            ItemRecord(
              id.toString,
              name,
              desc,
              workflowId.toString,
              seq,
              createdAt.asJavaZonedDateTime(),
              createdAt.asJavaZonedDateTime()
            )
          )
        )
    }
  }

  def list(implicit ec: ExecutionContext) =
    db.stream(ItemDao.sortBy(_.createdAt).result).mapResult(item => Item(UUID.fromString(item.id), item.name,item.desc, UUID.fromString(item.workflowId)))

}
