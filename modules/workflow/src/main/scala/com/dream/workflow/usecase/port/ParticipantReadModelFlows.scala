package com.dream.workflow.usecase.port

import java.util.UUID

import com.dream.workflow.domain.ParticipantDto
import akka.NotUsed
import akka.stream.scaladsl.{Flow, Source}
import org.sisioh.baseunits.scala.time.TimePoint
import slick.basic.DatabasePublisher

import scala.concurrent.ExecutionContext

trait ParticipantReadModelFlows {

  def resolveLastSeqNrSource(implicit ec: ExecutionContext): Source[Long, NotUsed]

  def newItemFlow(implicit ec: ExecutionContext): Flow[(UUID, String, String, String, String, Long, TimePoint), Int, NotUsed]

  def list: DatabasePublisher[ParticipantDto]

}
