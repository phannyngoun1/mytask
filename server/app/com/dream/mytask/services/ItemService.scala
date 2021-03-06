package com.dream.mytask.services

import java.util.UUID

import com.dream.mytask.shared.data.ItemData
import com.dream.mytask.shared.data.ItemData._
import com.dream.mytask.shared.data.WorkflowData.FlowJson
import com.dream.workflow.usecase.ItemAggregateUseCase.Protocol._

import scala.concurrent.Future

trait ItemService { this: ApiServiceGlobal =>

  override def getItemInitData(): Future[ItemInitDataJs] = {
    val data = for {
      list <- getItemList()
      flowList <- getFlowList()
    } yield (list, flowList)

    data.map { item =>
      ItemInitDataJs(
        item._1.map(it => ItemJson(it.id, it.name, it.desc, it.initPayloadCode)),
        item._2.map(flow => FlowJson(flow.id, flow.name))
      )
    }
  }

  override def getItem(id: String): Future[ItemJson] = {

    getApiServiceResources.getItemAggregateUseCase.getItem(GetItemCmdRequest(UUID.fromString(id))).map {

     case res: GetItemCmdSuccess => ItemJson(res.id, res.name, res.desc, None)
     case _ => ItemJson(UUID.randomUUID(), "", None, None)
   }
  }

  override def newItem(name: String, flowId: String, desc: Option[String]): Future[String] = {

    getApiServiceResources.getItemAggregateUseCase.createItem(CreateItemCmdRequest(
      id = UUID.randomUUID(),
      name = name,
      desc = desc,
      workflowId = UUID.fromString(flowId)
    )) map {
      case res: CreateItemCmdSuccess => s"id: ${res.id}"
      case _ => "Failed"
    }
  }

  override def getItemList(): Future[List[ItemJson]] = {
    getApiServiceResources.getItemAggregateUseCase.list.map(_.map(item => ItemJson(item.id, item.name, item.desc, item.initPayload)))
  }

}
