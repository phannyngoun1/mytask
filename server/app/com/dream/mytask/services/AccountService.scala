package com.dream.mytask.services

import java.util.UUID

import com.dream.mytask.shared.data.AccountData._
import com.dream.workflow.usecase.AccountAggregateUseCase.Protocol._
import com.dream.workflow.usecase.ParticipantAggregateUseCase.Protocol.{GetParticipantCmdSuccess => GetPartSuccess, GetParticipantCmdReq => GetPart, _}

import scala.concurrent.Future

trait AccountService { this: ApiService =>

  override def getAcc(id: String): Future[AccountJson] = {

    accountUseCase.getAccount(GetAccountCmdReq(UUID.fromString(id))) map {
      case res:GetAccountCmdSuccess => AccountJson(res.id.toString, res.name, res.curParticipantId.map(_.toString))
      case _ => AccountJson("", "", None)
    }
  }

  override def newAccount(name: String, fullName: String, participantId: Option[UUID]): Future[String] = {

    accountUseCase.createAccount(CreateAccountCmdReq(UUID.randomUUID(), name, fullName, participantId)) map {
      case  CreateAccountCmdSuccess(id) => id.toString
      case _ => "Failed"
    }
  }

  override def getAccountList(): Future[List[AccountJson]] =
//    Future.successful(List(AccountJson("sss", "sss")))
    accountUseCase.list.map(_.map(acc=> AccountJson(acc.id.toString, acc.name, None)))

  override def getParticipant(id: String): Future[ParticipantJson] = {
    participantUseCase.getParticipant(GetPart(UUID.fromString(id))) map {
      case res: GetPartSuccess => {

        println(s"participant : ${res}")

        ParticipantJson(res.id.toString, res.accountId.toString)
      }
      case _ => ParticipantJson("", "")
    }
  }

  override def newParticipant(accId: String): Future[String] = {
    val accountId = UUID.fromString(accId)

    participantUseCase.createParticipant(CreateParticipantCmdReq(UUID.randomUUID(), accountId, accountId, accountId, accountId)) map {
      case CreateParticipantCmdSuccess(id) => id.toString
      case _ => ""
    }
  }

  override def getParticipantList(): Future[List[ParticipantJson]] = {

    participantUseCase.list.map(_.map(item => ParticipantJson(item.id.toString, item.accountId.toString)))
  }


}
