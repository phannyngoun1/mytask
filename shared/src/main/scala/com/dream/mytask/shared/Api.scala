package com.dream.mytask.shared

import java.util.UUID

import com.dream.mytask.shared.data.AccountData.{AccountJson, ParticipantJson}
import com.dream.mytask.shared.data.ItemData.ItemJson
import com.dream.mytask.shared.data.ProcessInstanceData.ProcessInstanceJson
import com.dream.mytask.shared.data.TaskItem
import com.dream.mytask.shared.data.WorkflowData.FlowJson

import scala.concurrent.Future

trait Api {

  def welcomeMessage(smg: String):  Future[String]

  def getUser(id: String): Future[String]

  def newItem(name: String, flowId: String, desc: String): Future[String]

  def getItem(id: String) : Future[ItemJson]

  def getItemList(): Future[List[ItemJson]]

  def getTasks(accId: String): Future[List[TaskItem]]

  def createProcessInstance(itemId: String, submitter: String): Future[String]

  def getPInstanceList(): Future[List[ProcessInstanceJson]]

  def getProcessInstance(id: String): Future[String]

  def getFlow(id: String): Future[FlowJson]

  def newFlow(name: String,  participants: List[String]): Future[String]

  def getFlowList(): Future[List[FlowJson]]

  def getAcc(id: String): Future[AccountJson]

  def newAccount(name: String, fullName: String, participantId: Option[UUID] = None ): Future[String]

  def getAccountList(): Future[List[AccountJson]]

  def getParticipant(id: String): Future[ParticipantJson]

  def newParticipant(accId: String): Future[String]

  def getParticipantList(): Future[List[ParticipantJson]]

}
