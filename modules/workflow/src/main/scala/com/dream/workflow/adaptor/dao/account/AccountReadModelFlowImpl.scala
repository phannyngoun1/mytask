package com.dream.workflow.adaptor.dao.account

import java.util.UUID

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Source}
import com.dream.common.dto.workflow.Account.AccountDto
import com.dream.workflow.usecase.port.AccountReadModelFlow
import org.sisioh.baseunits.scala.time.TimePoint
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class AccountReadModelFlowImpl(val profile: JdbcProfile, val db: JdbcProfile#Backend#Database)
  extends AccountComponent with AccountReadModelFlow {

  import profile.api._

  override def resolveLastSeqNrSource(implicit ec: ExecutionContext): Source[Long, NotUsed] =
    Source.single(1).mapAsync(1) { _ =>
      db.run(AccountDao.map(_.sequenceNr).max.result)
        .map(_.getOrElse(0L))
    }

  override def newAccountFlow(implicit ec: ExecutionContext): Flow[(UUID, String, String, Long, TimePoint), Int, NotUsed] =
    Flow[(UUID, String, String, Long, TimePoint)].mapAsync(1) {
      case (id, name, fullName, seq, createdAt) =>
        db.run(
          AccountDao.forceInsert(
            AccountRecord(id.toString, name, fullName, seq, createdAt.asJavaZonedDateTime(), createdAt.asJavaZonedDateTime(), true))
          )
    }

  override def list =
    db.stream(AccountDao.sortBy(_.createdAt).result).mapResult(item => AccountDto(UUID.fromString(item.id), item.name, item.fullName))

  def getAccount(name: String)(implicit ec: ExecutionContext): Future[Option[AccountDto]] = {
    db.run(
      AccountDao.filter(_.name === name)
        .map(item=> (item.id, item.name, item.fullName))
        .result.headOption.map( _.map(item => AccountDto(UUID.fromString(item._1), item._2, item._3)))
    )
  }
}
