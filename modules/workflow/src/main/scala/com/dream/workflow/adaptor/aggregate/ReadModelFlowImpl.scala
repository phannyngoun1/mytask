package com.dream.workflow.adaptor.aggregate

import java.util.UUID

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Source}
import com.dream.workflow.usecase.port.ReadModelFlow
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class ReadModelFlowImpl(val profile: JdbcProfile, val db: JdbcProfile#Backend#Database) extends ReadModelFlow{
  override def resolveLastSeqNrSource(implicit ec: ExecutionContext): Source[Long, NotUsed] =
    Source.single(1).mapAsync(1) { _ =>
      Future.successful(0L)
    }

  override def newItemFlow: Flow[(UUID, String,  Long), Int, NotUsed] = {
    Flow[(UUID, String, Long)].mapAsync(1) {
      case (id, name, seq) =>
        println(s"new item id: ${id.toString}, name: ${name}, sq: ${seq}")
        Future.successful(1)
    }
  }
}
