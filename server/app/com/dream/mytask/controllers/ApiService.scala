package com.dream.mytask.controllers

import java.util.UUID

import com.dream.mytask.shared.Api
import javax.inject.{Inject, Singleton}


@Singleton
class ApiService @Inject()() extends Api {

  override def welcomeMessage(smg: String): String = s"welcome $smg - ${UUID.randomUUID()}"


}