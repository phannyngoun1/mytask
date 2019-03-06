package com.dream.mytask.controllers

import java.util.UUID

import com.dream.mytask.shared.data.ItemData._

import scala.concurrent.Future

trait ItemService { this: ApiService =>

  override def getItem(id: String): Future[Item] = {
    Future.successful(new Item(id,"Test"))
  }

  override def newItem(): Future[String] = {
    Future.successful("Nice")
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
