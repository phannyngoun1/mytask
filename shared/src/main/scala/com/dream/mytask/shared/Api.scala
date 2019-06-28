package com.dream.mytask.shared

import java.util.UUID

import com.dream.mytask.shared.data.AccountData.{AccountJson, ParticipantJson}
import com.dream.mytask.shared.data.ItemData.{ItemInitDataJs, ItemJson}
import com.dream.mytask.shared.data.ProcessInstanceData.{PInstInitDataInfoJs, PInstInitDataJson, ProcessInstanceJson}
import com.dream.mytask.shared.data.TaskItemJson
import com.dream.mytask.shared.data.WorkflowData.{FlowInitDataJs, FlowJson, PayloadJs, WorkflowTemplateJs}

import scala.concurrent.Future

trait Api extends TicketApi{

  def welcomeMessage(smg: String):  Future[String]

  def getUser(id: String): Future[String]

  def getItemInitData(): Future[ItemInitDataJs]

  def newItem(name: String, flowId: String, desc: Option[String]): Future[String]

  def getItem(id: String) : Future[ItemJson]

  def getItemList(): Future[List[ItemJson]]

  def getTasks(accId: String): Future[List[TaskItemJson]]

  def takeAction(pInstId: String, taskId: String, accId: String, participantId: String, action: String, payload: PayloadJs) : Future[String]

  def createProcessInstance(itemId: UUID, submitter: UUID): Future[String]

  def getPInstInitDat(): Future[PInstInitDataJson]

  def getPInstanceList(): Future[List[ProcessInstanceJson]]

  def getPInstDetail(pInstId: UUID, taskId: UUID, accId: UUID, participantId: UUID): Future[PInstInitDataInfoJs]

  def getProcessInstance(id: String): Future[String]

  def getFlow(id: UUID): Future[WorkflowTemplateJs]

  def newFlow(workflow: WorkflowTemplateJs): Future[String]

  def getFlowInitData(): Future[FlowInitDataJs]

  def getFlowList(): Future[List[FlowJson]]

  def getFlowTemplate(id: UUID): Future[WorkflowTemplateJs]

  def getAcc(id: String): Future[AccountJson]

  def newAccount(name: String, fullName: String, participantId: Option[UUID] = None ): Future[String]

  def getAccountList(): Future[List[AccountJson]]

  def getParticipant(id: String): Future[ParticipantJson]

  def newParticipant(accId: String): Future[String]

  def getParticipantList(): Future[List[ParticipantJson]]



}
