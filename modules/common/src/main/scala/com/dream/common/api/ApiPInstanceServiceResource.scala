package com.dream.common.api

import java.util.UUID

import scala.concurrent.Future

trait ApiPInstanceServiceResource {

  def createProcessInstance(itemId: UUID, submitter: UUID): Future[String]

  def getProcessInstance(id: String): Future[String]

  def getPInstanceList(): Future[List[ProcessInstanceDto]]
}
