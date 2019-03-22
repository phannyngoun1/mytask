package com.dream.workflow.usecase.port

import java.util.UUID

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Source}
import com.dream.workflow.domain.ProcessInstanceDto
import org.sisioh.baseunits.scala.time.TimePoint
import slick.basic.DatabasePublisher

import scala.concurrent.ExecutionContext

trait PInstanceReadModelFlows {

  def resolveLastSeqNrSource(implicit ec: ExecutionContext): Source[Long, NotUsed]

  def newPInst(implicit ec: ExecutionContext): Flow[(UUID, String, Long, TimePoint), Int, NotUsed]

  def list: DatabasePublisher[ProcessInstanceDto]
}
