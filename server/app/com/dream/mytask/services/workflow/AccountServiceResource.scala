package com.dream.mytask.services.workflow

import java.util.UUID

import com.dream.common.dto.workflow.Account._
import com.dream.mytask.services.ApiServiceResources
import com.dream.workflow.usecase.AccountAggregateUseCase.Protocol._
import com.dream.workflow.usecase.ParticipantAggregateUseCase.Protocol.{GetParticipantCmdSuccess => GetPartSuccess, GetParticipantCmdReq => GetPart, _}

import scala.concurrent.Future

trait AccountServiceResource { this: ApiServiceResources =>

  override def getAcc(id: String): Future[Option[AccountDto]] =
    getAccountAggregateUseCase.getAccount(GetAccountCmdReq(UUID.fromString(id))) map {
      case res:GetAccountCmdSuccess => Some(AccountDto(res.id, res.name, res.fullName))
      case _ => None
    }

  override def newAccount(name: String, fullName: String, participantId: Option[UUID]): Future[String] =
    getAccountAggregateUseCase.createAccount(CreateAccountCmdReq(UUID.randomUUID(), name, fullName, participantId)) map {
      case  CreateAccountCmdSuccess(id) => id.toString
      case _ => "Failed"
    }

  override def getAccountList(): Future[List[AccountDto]] =
    getAccountAggregateUseCase.list.map(_.map(acc=> AccountDto(acc.id, acc.name, acc.fullName)))

  override def newParticipant(accId: UUID): Future[String] =
    getParticipantAggregateUseCase.createParticipant(CreateParticipantCmdReq(UUID.randomUUID(), accId, accId, accId, accId)) map {
      case CreateParticipantCmdSuccess(id) => id.toString
      case _ => ""
    }

  override def getParticipant(id: UUID): Future[Option[ParticipantDto]] =
    getParticipantAggregateUseCase.getParticipant(GetPart(id)) map {
      case res: GetPartSuccess =>
        Some(ParticipantDto(res.id, res.accountId))
      case _ => None
    }

  override def getParticipantList(): Future[List[ParticipantDto]] =
    getParticipantAggregateUseCase.list.map(_.map(item => ParticipantDto(item.id, item.accountId)))

}
