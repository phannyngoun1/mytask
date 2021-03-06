package com.dream.workflow.usecase.port

import java.util.UUID

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Source}
import com.dream.workflow.domain.Item
import org.sisioh.baseunits.scala.time.TimePoint
import slick.basic.DatabasePublisher

import scala.concurrent.{ExecutionContext, Future}

trait ItemReadModelFlow {

  def resolveLastSeqNrSource(implicit ec: ExecutionContext): Source[Long, NotUsed]

  def newItemFlow(implicit ec: ExecutionContext): Flow[(UUID, String, Option[String], UUID, Long, TimePoint), Int, NotUsed]

  def list(implicit ec: ExecutionContext): DatabasePublisher[Item]

}
