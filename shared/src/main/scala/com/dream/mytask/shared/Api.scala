package com.dream.mytask.shared

import com.dream.mytask.shared.data.Task

import scala.concurrent.Future

trait Api {
  def welcomeMessage(smg: String):  Future[String]
  def getUser(id: String): Future[String]

  def getTasks(accId: String): Future[List[Task]]

  def createProcessInstance(): Future[String]
}
