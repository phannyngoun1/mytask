package com.dream.mytask.services

import com.dream.mytask.shared.data.AccountData._

import scala.concurrent.Future

trait AccountService { this: ApiService =>

  override def getAcc(id: String): Future[AccountJson] = ???

  override def newAccount(name: String, desc: String): Future[String] = ???

  override def getAccountList(): Future[List[AccountJson]] = ???

  override def getParticipant(id: String): Future[ParticipantJson] = ???

  override def newParticipant(id: String): Future[String] = ???

  override def getParticipantList(): Future[List[AccountJson]] = ???

}
