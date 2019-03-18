package com.dream.mytask.services

import java.util.UUID

import akka.actor.ActorSystem
import com.dream.mytask.shared.Api
import com.dream.mytask.shared.data.{AccountData, ActionItem, TaskItem, WorkflowData}
import com.dream.workflow.adaptor.aggregate._
import com.dream.workflow.adaptor.dao.flow.FlowReadModelFlowImpl
import com.dream.workflow.adaptor.dao.item.ItemReadModelFlowImpl
import com.dream.workflow.adaptor.journal.JournalReaderImpl
import com.dream.workflow.usecase.AccountAggregateUseCase.Protocol.{GetAccountCmdReq, GetAccountCmdSuccess, _}
import com.dream.workflow.usecase.ProcessInstanceAggregateUseCase.Protocol.{CreatePInstCmdRequest, CreatePInstCmdSuccess, GetPInstCmdRequest, GetPInstCmdSuccess}
import com.dream.workflow.usecase._
import com.typesafe.config.ConfigFactory
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}


class ApiService(login: UUID)(implicit val ec: ExecutionContext, implicit val  system: ActorSystem)
  extends Api
    with ItemService
    with FlowService
    with AccountService{


  val rootConfig = ConfigFactory.load()
  val dbConfig = DatabaseConfig.forConfig[JdbcProfile](path = "slickR", rootConfig)
  val readSideFlow = new ItemReadModelFlowImpl(dbConfig.profile, dbConfig.db)
  val flowReadModelFlow = new FlowReadModelFlowImpl(dbConfig.profile, dbConfig.db)

  val localEntityAggregates = system.actorOf(LocalEntityAggregates.props, LocalEntityAggregates.name)

  val itemFlow = new ItemAggregateFlowsImpl(localEntityAggregates)
  val workFlow = new WorkflowAggregateFlowsImpl(localEntityAggregates)
  val pInstFlow = new ProcessInstanceAggregateFlowsImpl(localEntityAggregates)
  val accountFlow = new AccountAggregateFlowsImpl(localEntityAggregates)
  val participantFlow = new ParticipantAggregateFlowsImpl(localEntityAggregates)
  val itemAggregateUseCase = new ItemAggregateUseCase(itemFlow,readSideFlow )
  val workflowAggregateUseCase = new WorkflowAggregateUseCase(workFlow, flowReadModelFlow)
  val processInstance = new ProcessInstanceAggregateUseCase(pInstFlow, workFlow, itemFlow, participantFlow)
  val accountUseCase = new AccountAggregateUseCase(accountFlow,participantFlow, pInstFlow )
  val participantUseCase = new ParticipantAggregateUseCase(participantFlow)



  val ex = new ReadModelUseCase(readSideFlow, flowReadModelFlow, new JournalReaderImpl(system))
  ex.executeItem()
  ex.executeFlow()

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

  override def getTasks(accId: String): Future[List[TaskItem]] = {

    val uuId = UUID.fromString(accId)

    accountUseCase.getTasks(GetTaskLisCmdReq(uuId)) map {
      case res: GetTaskListCmdSuccess => res.taskList.map( f => TaskItem(f.id, f.activity.name, f.actions.map(a => ActionItem(a.name))) )
      case _ => List.empty[TaskItem]
    }
  }

  override def createProcessInstance(): Future[String] = {

    processInstance.createPInst(CreatePInstCmdRequest(
      itemID = UUID.fromString("8c557884-0d50-4ba1-aa82-26b49b5be368"),
      by = UUID.fromString("8dbd6bf8-2f60-4e6e-8e3f-b374e060a940")
    )) map {
      case res: CreatePInstCmdSuccess => s"Process instance ${res.folio} created"
      case _ => "Failed"
    }
  }

  override def getProcessInstance(id: String): Future[String] = {
    val uuId = UUID.fromString(id)

    processInstance.getPInst(GetPInstCmdRequest(uuId))  map {
      case res: GetPInstCmdSuccess => res.folio
      case _ => s"Failed to fetch ${id}"
    }
  }

}