package com.dream.mytask.services

import akka.actor.ActorRef
import com.dream.common.api.{ApiAccountService, ApiItemService}
import com.dream.workflow.usecase.{AccountAggregateUseCase, ItemAggregateUseCase, ParticipantAggregateUseCase, ProcessInstanceAggregateUseCase, WorkflowAggregateUseCase}
import com.dream.workflow.usecase.port._
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.ExecutionContext

trait ApiServiceResources
  extends ApiAccountService
    with ApiItemService {

  implicit val ec: ExecutionContext

  def getLocalEntityAggregates: ActorRef

  def getDbConfigProvider: DatabaseConfigProvider

  def getItemReadModelFlow: ItemReadModelFlow

  def getFlowReadModelFlow: FlowReadModelFlow

  def getAccountReadModelFlow: AccountReadModelFlow

  def getParticipantReadModelFlows: ParticipantReadModelFlows

  def getPInstanceReadModelFlows: PInstanceReadModelFlows

  def getFlagReadModelFlows: FlagReadModelFlows

  def getItemAggregateFlows: ItemAggregateFlows

  def getWorkflowAggregateFlows: WorkflowAggregateFlows

  def getProcessInstanceAggregateFlows: ProcessInstanceAggregateFlows

  def getAccountAggregateFlows: AccountAggregateFlows

  def getParticipantAggregateFlows: ParticipantAggregateFlows

  def getItemAggregateUseCase: ItemAggregateUseCase

  def getWorkflowAggregateUseCase: WorkflowAggregateUseCase

  def getProcessInstanceAggregateUseCase: ProcessInstanceAggregateUseCase

  def getAccountAggregateUseCase: AccountAggregateUseCase

  def getParticipantAggregateUseCase: ParticipantAggregateUseCase

}
