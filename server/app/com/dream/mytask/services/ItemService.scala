package com.dream.mytask.services

import java.util.UUID

import com.dream.mytask.shared.data.ItemData._
import com.dream.workflow.usecase.ItemAggregateUseCase.Protocol._

import scala.concurrent.Future

trait ItemService { this: ApiService =>

  override def getItem(id: String): Future[ItemJson] = {

   itemAggregateUseCase.getItem(GetItemCmdRequest(UUID.fromString(id))).map {

     case res: GetItemCmdSuccess => ItemJson(res.id.toString, res.name)
     case _ => ItemJson("", "")
   }
  }

  override def newItem(name: String, desc: String): Future[String] = {
    val flowId = UUID.fromString("ad1ccc6e-b805-49d2-b2bc-ecb37333f25e")

    itemAggregateUseCase.createItem(CreateItemCmdRequest(
      id = UUID.randomUUID(),
      name = name,
      desc = desc,
      flowId
    )) map {
      case res: CreateItemCmdSuccess => s"id: ${res.id}"
      case _ => "Failed"
    }
  }

  override def getItemList(): Future[List[ItemJson]] = {
    itemAggregateUseCase.list.map(_.map(item => ItemJson(item.id.toString, item.name)))
  }

}
