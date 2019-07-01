package com.dream.mytask.services

import akka.actor.{ActorRef, ActorSystem}
import com.dream.mytask.services.workflow.AccountServiceResource
import com.dream.workflow.adaptor.aggregate.{AccountAggregateFlowsImpl, ItemAggregateFlowsImpl, LocalEntityAggregates, ParticipantAggregateFlowsImpl, ProcessInstanceAggregateFlowsImpl, WorkflowAggregateFlowsImpl}
import com.dream.workflow.adaptor.dao.account.AccountReadModelFlowImpl
import com.dream.workflow.adaptor.dao.flag.FlagReadModelFlowImpl
import com.dream.workflow.adaptor.dao.flow.FlowReadModelFlowImpl
import com.dream.workflow.adaptor.dao.item.ItemReadModelFlowImpl
import com.dream.workflow.adaptor.dao.participant.ParticipantReadModelFlowImpl
import com.dream.workflow.adaptor.dao.processinstance.PInstanceReadModelFlowImpl
import com.dream.workflow.usecase.port.{AccountAggregateFlows, AccountReadModelFlow, FlagReadModelFlows, FlowReadModelFlow, ItemAggregateFlows, ItemReadModelFlow, PInstanceReadModelFlows, ParticipantAggregateFlows, ParticipantReadModelFlows, ProcessInstanceAggregateFlows, WorkflowAggregateFlows}
import com.dream.workflow.usecase.{AccountAggregateUseCase, ItemAggregateUseCase, ParticipantAggregateUseCase, ProcessInstanceAggregateUseCase, WorkflowAggregateUseCase}
import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class ServiceResources @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit val ec: ExecutionContext, implicit val  system: ActorSystem)
  extends ApiServiceResources
    with AccountServiceResource {


  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  val db = dbConfig.db
  val itemReadModelFlow = new ItemReadModelFlowImpl(dbConfig.profile, db)
  val flowReadModelFlow = new FlowReadModelFlowImpl(dbConfig.profile, db)
  val accountReadModelFlow = new AccountReadModelFlowImpl(dbConfig.profile, db)
  val participantReadModelFlows = new ParticipantReadModelFlowImpl(dbConfig.profile, db)
  val pInstanceReadModelFlows = new PInstanceReadModelFlowImpl(dbConfig.profile, db)
  val flagReadModelFlows = new FlagReadModelFlowImpl(dbConfig.profile, db)


  val localEntityAggregates = system.actorOf(LocalEntityAggregates.props, LocalEntityAggregates.name)

  val itemFlow = new ItemAggregateFlowsImpl(localEntityAggregates)
  val workFlow = new WorkflowAggregateFlowsImpl(localEntityAggregates)
  val pInstFlow = new ProcessInstanceAggregateFlowsImpl(localEntityAggregates)
  val accountFlow = new AccountAggregateFlowsImpl(localEntityAggregates)
  val participantFlow = new ParticipantAggregateFlowsImpl(localEntityAggregates)

  val itemAggregateUseCase = new ItemAggregateUseCase(itemFlow, workFlow,itemReadModelFlow )
  val workflowAggregateUseCase = new WorkflowAggregateUseCase(workFlow, flowReadModelFlow)
  val processInstance = new ProcessInstanceAggregateUseCase(pInstFlow, workFlow, itemFlow, participantFlow, pInstanceReadModelFlows)
  val accountUseCase = new AccountAggregateUseCase(accountFlow,participantFlow, pInstFlow, accountReadModelFlow, workFlow )
  val participantUseCase = new ParticipantAggregateUseCase(participantFlow, accountFlow , participantReadModelFlows)


  sys.addShutdownHook {
    db.close()
    db.shutdown
    system.terminate()
  }


  override def getDbConfigProvider: DatabaseConfigProvider = dbConfigProvider

  override def getItemReadModelFlow: ItemReadModelFlow = itemReadModelFlow

  override def getFlowReadModelFlow: FlowReadModelFlow = flowReadModelFlow

  override def getAccountReadModelFlow: AccountReadModelFlow = accountReadModelFlow

  override def getParticipantReadModelFlows: ParticipantReadModelFlows = participantReadModelFlows

  override def getPInstanceReadModelFlows: PInstanceReadModelFlows = pInstanceReadModelFlows

  override def getFlagReadModelFlows: FlagReadModelFlows = flagReadModelFlows

  override def getItemAggregateFlows: ItemAggregateFlows = itemFlow

  override def getWorkflowAggregateFlows: WorkflowAggregateFlows = workFlow

  override def getProcessInstanceAggregateFlows: ProcessInstanceAggregateFlows = pInstFlow

  override def getAccountAggregateFlows: AccountAggregateFlows = accountFlow

  override def getParticipantAggregateFlows: ParticipantAggregateFlows = participantFlow

  override def getItemAggregateUseCase: ItemAggregateUseCase = itemAggregateUseCase

  override def getWorkflowAggregateUseCase: WorkflowAggregateUseCase = workflowAggregateUseCase

  override def getProcessInstanceAggregateUseCase: ProcessInstanceAggregateUseCase = processInstance

  override def getAccountAggregateUseCase: AccountAggregateUseCase = accountUseCase

  override def getParticipantAggregateUseCase: ParticipantAggregateUseCase = participantUseCase

  override def getLocalEntityAggregates: ActorRef = localEntityAggregates
}
