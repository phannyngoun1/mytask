package com.dream.mytask.shared

import com.dream.mytask.shared.data.ItemData.ItemJson
import com.dream.mytask.shared.data.TaskItem
import com.dream.mytask.shared.data.WorkflowData.FlowJson

import scala.concurrent.Future

trait Api {

  def welcomeMessage(smg: String):  Future[String]

  def getUser(id: String): Future[String]

  def newItem(name: String, desc: String): Future[String]

  def getItem(id: String) : Future[ItemJson]

  def getItemList(): Future[List[ItemJson]]

  def getTasks(accId: String): Future[List[TaskItem]]

  def createProcessInstance(): Future[String]

  def getProcessInstance(id: String): Future[String]

  def getFlow(id: String): Future[FlowJson]

  def newFlow(name: String): Future[String]

}
