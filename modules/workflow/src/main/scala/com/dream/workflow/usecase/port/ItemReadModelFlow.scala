package com.dream.workflow.usecase.port

import java.util.UUID

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Source}

import scala.concurrent.ExecutionContext

trait ItemReadModelFlow {

  def resolveLastSeqNrSource(implicit ec: ExecutionContext): Source[Long, NotUsed]

  def newItemFlow: Flow[(UUID, String, Long), Int, NotUsed]


}
