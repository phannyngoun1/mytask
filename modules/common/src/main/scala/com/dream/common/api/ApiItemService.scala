package com.dream.common.api

import scala.concurrent.Future

trait ApiItemService {

  def getItem(id: String): Future[ItemDto]

  def newItem(name: String, flowId: String, desc: Option[String]): Future[String]

  def getItemList(): Future[List[ItemDto]]



}
