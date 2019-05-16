package com.dream.mytask.shared

import java.util.UUID

import com.dream.mytask.shared.data.AccountData.{AccountJson, ParticipantJson}
import com.dream.mytask.shared.data.ItemData.{ItemInitDataJs, ItemJson}
import com.dream.mytask.shared.data.ProcessInstanceData.{PInstInitDataJson, ProcessInstanceJson}
import com.dream.mytask.shared.data.TaskItemJson
import com.dream.mytask.shared.data.WorkflowData.{EditTicketPayloadJs, FlowInitDataJs, FlowJson, PayloadJs}

import scala.concurrent.Future

trait Api {

  def welcomeMessage(smg: String):  Future[String]

  def getUser(id: String): Future[String]

  def getItemInitData(): Future[ItemInitDataJs]

  def newItem(name: String, flowId: String, desc: String): Future[String]

  def getItem(id: String) : Future[ItemJson]

  def getItemList(): Future[List[ItemJson]]

  def getTasks(accId: String): Future[List[TaskItemJson]]

  def takeAction(pInstId: String, taskId: String, accId: String, participantId: String, action: String, payload: PayloadJs) : Future[String]

  def createProcessInstance(itemId: String, submitter: String): Future[String]

  def getPInstInitDat(): Future[PInstInitDataJson]

  def getPInstanceList(): Future[List[ProcessInstanceJson]]

  def getProcessInstance(id: String): Future[String]

  def getFlow(id: String): Future[FlowJson]

  def newFlow(name: String,  participants: List[String]): Future[String]

  def getFlowInitData(): Future[FlowInitDataJs]

  def getFlowList(): Future[List[FlowJson]]

  def getAcc(id: String): Future[AccountJson]

  def newAccount(name: String, fullName: String, participantId: Option[UUID] = None ): Future[String]

  def getAccountList(): Future[List[AccountJson]]

  def getParticipant(id: String): Future[ParticipantJson]

  def newParticipant(accId: String): Future[String]

  def getParticipantList(): Future[List[ParticipantJson]]


}
