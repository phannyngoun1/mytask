package com.dream.mytask.services

import java.util.UUID

import akka.actor.ActorSystem
import com.dream.mytask.shared.Api
import com.dream.mytask.shared.data.WorkflowData.{EditTicketPayloadJs, PayloadJs}
import com.dream.mytask.shared.data._
import com.dream.workflow.adaptor.aggregate._
import com.dream.workflow.adaptor.dao.account.AccountReadModelFlowImpl
import com.dream.workflow.adaptor.dao.flag.FlagReadModelFlowImpl
import com.dream.workflow.adaptor.dao.flow.FlowReadModelFlowImpl
import com.dream.workflow.adaptor.dao.item.ItemReadModelFlowImpl
import com.dream.workflow.adaptor.dao.participant.ParticipantReadModelFlowImpl
import com.dream.workflow.adaptor.dao.processinstance.PInstanceReadModelFlowImpl
import com.dream.workflow.adaptor.journal.JournalReaderImpl
import com.dream.workflow.domain.Action
import com.dream.workflow.usecase.AccountAggregateUseCase.Protocol.{GetAccountCmdReq, GetAccountCmdSuccess, _}
import com.dream.workflow.usecase.ProcessInstanceAggregateUseCase.Protocol.{ActionCompleted, TakeActionCmdRequest}
import com.dream.workflow.usecase._
import com.typesafe.config.ConfigFactory
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import TicketPayloadConverter._
import com.dream.common.NonePayload

import scala.concurrent.{ExecutionContext, Future}


class ApiService(login: UUID)(implicit val ec: ExecutionContext, implicit val  system: ActorSystem)
  extends Api
    with PayloadConverter
    with ItemService
    with FlowService
    with AccountService
    with PInstanceService
    with TicketService {

  val rootConfig = ConfigFactory.load()
  val dbConfig = DatabaseConfig.forConfig[JdbcProfile](path = "slickR", rootConfig)
  val db = dbConfig.db
  val readSideFlow = new ItemReadModelFlowImpl(dbConfig.profile, db)
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
    flagReadModelFlows,
    new JournalReaderImpl(system)
  )

  ex.execute

  sys.addShutdownHook {
    db.close()
    db.shutdown
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

    accountUseCase.getTasks(GetTaskLisCmdReq(uuId)) map (_.map { f =>
      println(f)
      TaskItemJson(f.id.toString, f.pInstId.toString, f.participantId.toString, f.activity.name, f.actions.map(a => ActionItemJson(a.name)))
    })
  }

  override def takeAction(pInstId: String, taskId: String, accId: String, participantId: String, actionName: String, payload: PayloadJs): Future[String] = {

    val pInstIdUUID = UUID.fromString(pInstId)
    val taskIdUUID = UUID.fromString(taskId)
    val participantUUI = UUID.fromString(participantId)

    processInstance.takeAction(TakeActionCmdRequest(pInstIdUUID, taskIdUUID, actionName, participantUUI, convertEditTicketPayload(payload), payload.comment )).map {
      case _ => "Completed"
    }
  }
}