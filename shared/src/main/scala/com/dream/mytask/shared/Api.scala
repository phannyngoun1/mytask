package com.dream.mytask.shared

trait Api {
  def welcomeMessage(smg: String): String
  def getUser(id: String): String
}
