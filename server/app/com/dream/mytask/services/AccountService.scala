package com.dream.mytask.services

import java.util.UUID

import com.dream.mytask.shared.data.AccountData._
import com.dream.workflow.usecase.AccountAggregateUseCase.Protocol._
import com.dream.workflow.usecase.ParticipantAggregateUseCase.Protocol.{GetParticipantCmdSuccess, _}

import scala.concurrent.Future

trait AccountService { this: ApiService =>

  override def getAcc(id: String): Future[AccountJson] = {

    accountUseCase.getAccount(GetAccountCmdReq(UUID.fromString(id))) map {
      case res:GetAccountCmdSuccess => AccountJson(res.id.toString, res.name)
      case _ => AccountJson("", "")
    }
  }

  override def newAccount(name: String, fullName: String, participantId: Option[UUID]): Future[String] = {

    accountUseCase.createAccount(CreateAccountCmdReq(UUID.randomUUID(), name, fullName, participantId)) map {
      case  CreateAccountCmdSuccess(id) => id.toString
      case _ => "Failed"
    }
  }

  override def getAccountList(): Future[List[AccountJson]] =
    accountUseCase.list.map(_.map(acc=> AccountJson(acc.id.toString, acc.name)))

  override def getParticipant(id: String): Future[ParticipantJson] = {
    participantUseCase.getParticipant(GetParticipantCmdReq(UUID.fromString(id))) map {
      case res: GetParticipantCmdSuccess => ParticipantJson(res.id.toString)
      case _ => ParticipantJson("")
    }
  }

  override def newParticipant(id: String): Future[String] = {
    val id = UUID.fromString(id)
    participantUseCase.createParticipant(CreateParticipantCmdReq(id, id, id, id, id)) map {
      case CreateParticipantCmdSuccess(id) => id.toString
      case _ => ""
    }
  }

  override def getParticipantList(): Future[List[ParticipantJson]] = {
    participantUseCase.list.map(_.map(item => ParticipantJson(item.id.toString)))
  }


}
