package com.dream.mytask.shared

import com.dream.mytask.shared.data.ItemData.Item
import com.dream.mytask.shared.data.TaskItem

import scala.concurrent.Future

trait Api {
  def welcomeMessage(smg: String):  Future[String]

  def getUser(id: String): Future[String]

  def newItem(): Future[String]

  def getItem(id: String) : Future[Item]

  def getItemList(): Future[List[Item]]

  def getTasks(accId: String): Future[List[TaskItem]]

  def createProcessInstance(): Future[String]

  def getProcessInstance(id: String): Future[String]
}
