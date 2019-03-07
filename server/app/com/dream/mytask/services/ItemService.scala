package com.dream.mytask.services

import java.util.UUID

import com.dream.mytask.shared.data.ItemData._
import com.dream.workflow.usecase.ItemAggregateUseCase.Protocol.{CreateItemCmdRequest, CreateItemCmdSuccess}

import scala.concurrent.Future

trait ItemService { this: ApiService =>

  override def getItem(id: String): Future[Item] = {
    Future.successful(new Item(id,"Test"))
  }

  override def newItem(): Future[String] = {
    val flowId = UUID.fromString("ad1ccc6e-b805-49d2-b2bc-ecb37333f25e")
    itemAggregateUseCase.createItem(CreateItemCmdRequest(
      id = UUID.randomUUID(),
      name = "test",
      desc = "test",
      flowId
    )) map {
      case res: CreateItemCmdSuccess => s"id: ${res.id}"
      case _ => "Failed"
    }
  }

  override def getItemList(): Future[List[Item]] = {
    Future.successful(
      List(
        new Item(UUID.randomUUID().toString,"Test"),
        new Item(UUID.randomUUID().toString,"Test1")
      )
    )
  }

}
