package com.dream.mytask.services

import java.util.UUID

import com.dream.mytask.shared.data.ProcessInstanceData.ProcessInstanceJson
import com.dream.workflow.usecase.ProcessInstanceAggregateUseCase.Protocol.{CreatePInstCmdRequest, CreatePInstCmdSuccess, GetPInstCmdRequest, GetPInstCmdSuccess}

import scala.concurrent.Future

trait PInstanceService { this: ApiService =>

  override def createProcessInstance(itemId: String, submitter: String): Future[String] = {

    processInstance.createPInst(CreatePInstCmdRequest(
      itemID = UUID.fromString(itemId),
      by = UUID.fromString(submitter)
    )) map {
      case res: CreatePInstCmdSuccess => s"Process instance ${res.folio} created"
      case _ => "Failed"
    }
  }

  override def getProcessInstance(id: String): Future[String] = {
    val uuId = UUID.fromString(id)

    processInstance.getPInst(GetPInstCmdRequest(uuId))  map {
      case res: GetPInstCmdSuccess => s"id: ${res.id.toString}, folio: ${res.folio}, tasks : ${res.tasks.mkString(";")}"
      case _ => s"Failed to fetch ${id}"
    }
  }

  override def getPInstanceList(): Future[List[ProcessInstanceJson]] = {
    processInstance.list.map(_.map(item => ProcessInstanceJson(item.id.toString, item.folio)))
  }


}
