package com.dream.mytask.services

import com.dream.mytask.shared.data.WorkflowData

import scala.concurrent.Future

trait FlowService {  this: ApiService =>

  override def getFlow(id: String): Future[WorkflowData.FlowJson] = ???

}
