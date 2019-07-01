package com.dream.mytask.services

import com.dream.mytask.shared.Api

import scala.concurrent.ExecutionContext

trait ApiServiceGlobal extends Api{

  implicit val ec: ExecutionContext

  def getApiServiceResources: ApiServiceResources

}
