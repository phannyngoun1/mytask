package com.dream.workflow.usecase.port

import java.util.UUID

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Source}
import com.dream.workflow.domain.{FlowDto, Item}
import org.sisioh.baseunits.scala.time.TimePoint
import slick.basic.DatabasePublisher

import scala.concurrent.{ExecutionContext, Future}

trait FlowReadModelFlow {

  def resolveLastSeqNrSource(implicit ec: ExecutionContext): Source[Long, NotUsed]

  def newItemFlow(implicit ec: ExecutionContext): Flow[(UUID, String, Long, TimePoint), Int, NotUsed]

  def list: DatabasePublisher[FlowDto]

}
