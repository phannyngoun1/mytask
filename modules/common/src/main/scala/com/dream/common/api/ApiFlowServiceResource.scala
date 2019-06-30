package com.dream.common.api

import java.util.UUID

import scala.concurrent.Future

trait ApiFlowServiceResource {

  def getFlowTemplate(id: UUID): Future[FlowTemplateDto]

  def getFlow(id: UUID): Future[FlowDto]

  def getFlowList(): Future[List[FlowDto]]

  def newFlow(workflow: FlowTemplateDto): Future[String]

}
