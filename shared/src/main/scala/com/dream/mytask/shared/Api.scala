package com.dream.mytask.shared

import scala.concurrent.Future

trait Api {
  def welcomeMessage(smg: String):  Future[String]
  def getUser(id: String): Future[String]
}
