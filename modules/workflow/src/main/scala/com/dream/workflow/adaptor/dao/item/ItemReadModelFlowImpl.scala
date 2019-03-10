package com.dream.workflow.adaptor.dao.item

import java.util.UUID

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Source}
import com.dream.workflow.usecase.port.ItemReadModelFlow
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class ItemReadModelFlowImpl(val profile: JdbcProfile, val db: JdbcProfile#Backend#Database)
  extends ItemComponent
    with ItemReadModelFlow{

  import profile.api._
  override def resolveLastSeqNrSource(implicit ec: ExecutionContext): Source[Long, NotUsed] =
    Source.single(1).mapAsync(1) { _ =>
      Future.successful(0L)
    }

  override def newItemFlow: Flow[(UUID, String,  Long, TimePoint), Int, NotUsed] = {
    Flow[(UUID, String, Long)].mapAsync(1) {
      case (id, name, seq) =>
        println(s"new item id: ${id.toString}, name: ${name}, sq: ${seq}")
        db.run(
          ItemDao.forceInsert(
            ItemRecord(
              id, name, seq,
            )
          )
        )
        Future.successful(1)
    }
  }
}
