package com.dream.mytask.services

import java.util.UUID

import com.dream.mytask.shared.data.AccountData._

import scala.concurrent.Future

trait AccountService { this: ApiServiceGlobal =>

  override def getAcc(id: String): Future[Option[AccountJson]] =
    getApiServiceResources.getAcc(id).map(_.map(acc => AccountJson(acc.id.toString, acc.name, None)))

  override def newAccount(name: String, fullName: String, participantId: Option[UUID]): Future[String] =
    getApiServiceResources.newAccount(name, fullName, participantId)

  override def getAccountList(): Future[List[AccountJson]] =
    getApiServiceResources.getAccountList().map(_.map(acc =>  AccountJson(acc.id.toString, acc.name, None)))

  override def getParticipant(id: UUID): Future[Option[ParticipantJson]] =
    getApiServiceResources.getParticipant(id).map(_.map(pp => ParticipantJson(pp.id.toString, pp.accountId.toString, List.empty)))


  override def newParticipant(accId: UUID): Future[String] = {
    getApiServiceResources.newParticipant(accId)
  }

  override def getParticipantList(): Future[List[ParticipantJson]] =
    getApiServiceResources.getParticipantList().map(_.map(item => ParticipantJson(item.id.toString, item.accountId.toString, List())))
}
