package com.dream.mytask.services

import java.util.UUID

import akka.actor.ActorSystem
import com.dream.mytask.shared.Api
import com.dream.mytask.shared.data.{ActionItemJson, TaskItemJson}
import com.dream.workflow.adaptor.aggregate._
import com.dream.workflow.adaptor.dao.account.AccountReadModelFlowImpl
import com.dream.workflow.adaptor.dao.flow.FlowReadModelFlowImpl
import com.dream.workflow.adaptor.dao.item.ItemReadModelFlowImpl
import com.dream.workflow.adaptor.dao.participant.ParticipantReadModelFlowImpl
import com.dream.workflow.adaptor.dao.processinstance.PInstanceReadModelFlowImpl
import com.dream.workflow.adaptor.journal.JournalReaderImpl
import com.dream.workflow.usecase.AccountAggregateUseCase.Protocol.{GetAccountCmdReq, GetAccountCmdSuccess, _}
import com.dream.workflow.usecase._
import com.typesafe.config.ConfigFactory
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}


class ApiService(login: UUID)(implicit val ec: ExecutionContext, implicit val  system: ActorSystem)
  extends Api
    with ItemService
    with FlowService
    with AccountService
    with PInstanceService {

  val rootConfig = ConfigFactory.load()
  val dbConfig = DatabaseConfig.forConfig[JdbcProfile](path = "slickR", rootConfig)
  val readSideFlow = new ItemReadModelFlowImpl(dbConfig.profile, dbConfig.db)
  val flowReadModelFlow = new FlowReadModelFlowImpl(dbConfig.profile, dbConfig.db)
  val accountReadModelFlow = new AccountReadModelFlowImpl(dbConfig.profile, dbConfig.db)
  val participantReadModelFlows = new ParticipantReadModelFlowImpl(dbConfig.profile, dbConfig.db)
  val pInstanceReadModelFlows = new PInstanceReadModelFlowImpl(dbConfig.profile, dbConfig.db)


  val localEntityAggregates = system.actorOf(LocalEntityAggregates.props, LocalEntityAggregates.name)

  val itemFlow = new ItemAggregateFlowsImpl(localEntityAggregates)
  val workFlow = new WorkflowAggregateFlowsImpl(localEntityAggregates)
  val pInstFlow = new ProcessInstanceAggregateFlowsImpl(localEntityAggregates)
  val accountFlow = new AccountAggregateFlowsImpl(localEntityAggregates)
  val participantFlow = new ParticipantAggregateFlowsImpl(localEntityAggregates)
  val itemAggregateUseCase = new ItemAggregateUseCase(itemFlow,readSideFlow )
  val workflowAggregateUseCase = new WorkflowAggregateUseCase(workFlow, flowReadModelFlow)
  val processInstance = new ProcessInstanceAggregateUseCase(pInstFlow, workFlow, itemFlow, participantFlow, pInstanceReadModelFlows)
  val accountUseCase = new AccountAggregateUseCase(accountFlow,participantFlow, pInstFlow, accountReadModelFlow )
  val participantUseCase = new ParticipantAggregateUseCase(participantFlow, accountFlow , participantReadModelFlows)

  val ex = new ReadModelUseCase(
    readSideFlow,
    flowReadModelFlow,
    accountReadModelFlow,
    participantReadModelFlows ,
    pInstanceReadModelFlows,
    new JournalReaderImpl(system)
  )

  ex.executeItem
  ex.executeFlow
  ex.executeAcc
  ex.executeParticipant
  ex.executePInst

  sys.addShutdownHook {
    system.terminate()
  }

  override def welcomeMessage(smg: String): Future[String] = {
    Future.successful(s"welcome $smg - ${UUID.randomUUID()}")
  }

  override def getUser(id: String): Future[String] = {
    accountUseCase.getAccount(GetAccountCmdReq(UUID.fromString(id))).map {
      case res: GetAccountCmdSuccess => s"id: ${res.id}, name: ${res.name}, full name: ${res.fullName}, participant id: ${res.curParticipantId} "
      case _ => "Failed"
    }
  }

  override def getTasks(accId: String): Future[List[TaskItemJson]] = {

    val uuId = UUID.fromString(accId)

    accountUseCase.getTasks(GetTaskLisCmdReq(uuId)) map (_.map(f =>
      TaskItemJson(f.id.toString, f.pInstId.toString, f.participantId.toString, f.activity.name, f.actions.map(a => ActionItemJson(a.name)))
    ))
  }

  override def takeAction(pInstId: String, taskId: String, participantId: String, action: String): Future[List[TaskItemJson]] = ???
}