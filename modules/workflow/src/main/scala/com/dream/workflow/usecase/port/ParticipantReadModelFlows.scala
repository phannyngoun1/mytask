package com.dream.workflow.usecase.port

import java.util.UUID

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Source}
import com.dream.workflow.domain.Participant.ParticipantDto
import org.sisioh.baseunits.scala.time.TimePoint
import slick.basic.DatabasePublisher

import scala.concurrent.{ExecutionContext, Future}

trait ParticipantReadModelFlows {

  def resolveLastSeqNrSource(implicit ec: ExecutionContext): Source[Long, NotUsed]

  def newItemFlow(implicit ec: ExecutionContext): Flow[(UUID, UUID, UUID, UUID, UUID, Long, TimePoint), Int, NotUsed]

  def list: DatabasePublisher[ParticipantDto]

  def getParticipantByUser(id: UUID)(implicit ec: ExecutionContext): Future[Seq[ParticipantDto]]

}
