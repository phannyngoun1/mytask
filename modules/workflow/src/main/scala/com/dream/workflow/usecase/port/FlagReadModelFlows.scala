package com.dream.workflow.usecase.port

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Source}

import scala.concurrent.ExecutionContext

trait FlagReadModelFlows {

  def resolveLastSeqNrSource(implicit ec: ExecutionContext): Source[Long, NotUsed]

  def update(implicit ec: ExecutionContext): Flow[(String, Long), Int, NotUsed]
}
