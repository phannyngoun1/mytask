package com.dream.workflow.usecase.port

import java.util.UUID

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Source}
import com.dream.workflow.domain.Account.AccountDto
import org.sisioh.baseunits.scala.time.TimePoint
import slick.basic.DatabasePublisher

import scala.concurrent.{ExecutionContext, Future}

trait AccountReadModelFlow {

  def resolveLastSeqNrSource(implicit ec: ExecutionContext): Source[Long, NotUsed]

  def newAccountFlow(implicit ec: ExecutionContext): Flow[(UUID, String, String, Long, TimePoint), Int, NotUsed]

  def list: DatabasePublisher[AccountDto]

  def getAccount(name: String)(implicit ec: ExecutionContext): Future[Option[AccountDto]]

}
