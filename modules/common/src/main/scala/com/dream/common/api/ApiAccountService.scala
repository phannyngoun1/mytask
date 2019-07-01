package com.dream.common.api

import java.util.UUID

import com.dream.common.dto.workflow.Account.{AccountDto, ParticipantDto}

import scala.concurrent.Future

trait ApiAccountService {

  def getAcc(id: String): Future[Option[AccountDto]]

  def newAccount(name: String, fullName: String, participantId: Option[UUID]): Future[String]

  def getAccountList(): Future[List[AccountDto]]

  def newParticipant(accId: UUID): Future[String]

  def getParticipant(id: UUID): Future[Option[ParticipantDto]]

  def getParticipantList(): Future[List[ParticipantDto]]

}
