package com.dream.workflow.adaptor.aggregate

import akka.NotUsed
import akka.actor.ActorRef
import akka.pattern.ask
import akka.stream.scaladsl.Flow
import akka.util.Timeout
import com.dream.common.Protocol.CmdResponseFailed
import com.dream.common.domain.ResponseError
import com.dream.workflow.entity.account.AccountProtocol._
import com.dream.workflow.usecase.AccountAggregateUseCase.Protocol
import com.dream.workflow.usecase.port.AccountAggregateFlows

import scala.concurrent.duration._
import scala.language.postfixOps

class AccountAggregateFlowsImpl(aggregateRef: ActorRef) extends AccountAggregateFlows {

  private implicit val to: Timeout = Timeout(2 seconds)

  override def create: Flow[Protocol.CreateAccountCmdReq, Protocol.CreateAccountCmdRes, NotUsed] =
    Flow[Protocol.CreateAccountCmdReq]
      .map(req => CreateAccountCmdRequest(req.id, req.name, req.fullName, req.participantId))
      .mapAsync(1)(aggregateRef ? _)
      .map {
        case res: CreateAccountCmdSuccess => Protocol.CreateAccountCmdSuccess(res.id)
        case CmdResponseFailed(message) => Protocol.CreateAccountCmdFailed(ResponseError(message))
      }

  override def get: Flow[Protocol.GetAccountCmdReq, Protocol.GetAccountCmdRes, NotUsed] =
    Flow[Protocol.GetAccountCmdReq]
      .map(req => GetAccountCmdRequest(req.id))
      .mapAsync(1)(aggregateRef ? _)

      .map {
        case GetAccountCmdSuccess(account) => Protocol.GetAccountCmdSuccess(account.id, account.name, account.fullName, account.currParticipantId)
        case CmdResponseFailed(message) => Protocol.GetAccountCmdFailed(ResponseError(message))
        case _ =>  Protocol.GetAccountCmdFailed(ResponseError(None, "test"))
      }

  override def assignParticipant: Flow[Protocol.AssignParticipantCmdReq, Protocol.AssignParticipantCmdRes, NotUsed] =
    Flow[Protocol.AssignParticipantCmdReq]
      .map(req => AssignParticipantCmdRequest(req.id, req.participantId))
      .mapAsync(1)(aggregateRef ? _)
      .map {
        case AssignParticipantCmdSuccess(id) => Protocol.AssignParticipantCmdSuccess(id)
        case CmdResponseFailed(message) => Protocol.AssignParticipantCmdFailed(ResponseError(message))
      }

  override def getParticipant: Flow[Protocol.GetParticipantCmdReq, Protocol.GetParticipantCmdRes, NotUsed] = {
    Flow[Protocol.GetParticipantCmdReq]
      .map(req => GetParticipantCmdReq(req.accId))
      .mapAsync(1)(aggregateRef ? _)
      .map {
        case GetParticipantCmdSuccess(ids) => Protocol.GetParticipantCmdSuccess(ids)
        case CmdResponseFailed(message) => Protocol.GetParticipantCmdFailed(ResponseError(message))
      }
  }
}
