package com.dream.mytask.modules.item

import com.dream.mytask.services.{AjaxClient, MessageHandler}
import com.dream.mytask.shared.Api
import com.dream.mytask.shared.data.ItemData.Item
import diode._
import diode.data.{Empty, Pot, PotAction}
import diode.util.RunAfterJS
import boopickle.Default._
import autowire._
import com.dream.mytask.services.AppCircuit.zoomRW
import com.dream.mytask.services.DataModel.{ItemModel, RootModel}

object ItemActionHandler {

  case class FetchItemListAction(potResult: Pot[List[Item]] = Empty) extends PotAction[List[Item], FetchItemListAction] {
    override def next(newResult: Pot[List[Item]]): FetchItemListAction = FetchItemListAction(newResult)
  }

  case class FetchItemAction( id: Option[String], potResult: Pot[Item] = Empty) extends PotAction[Item, FetchItemAction] {
    override def next(newResult: Pot[Item]): FetchItemAction = FetchItemAction(None, newResult)
  }

  case class NewItemAction( potResult: Pot[String] = Empty) extends PotAction[String, NewItemAction] {
    override def next(newResult: Pot[String]): NewItemAction = NewItemAction(newResult)
  }

  def apply(circuit: Circuit[RootModel]) = circuit.composeHandlers(
    new FetchItemListActionHandler(zoomRW(_.itemModel.itemList)((m, v) => m.copy(itemModel = m.itemModel.copy(itemList = v)))),
    new FetchItemActionHandler(zoomRW(_.itemModel.item)((m, v) => m.copy(itemModel = m.itemModel.copy(item = v)) )),
    new ItemActionHandler(zoomRW(_.itemModel.message)((m, v) => m.copy(itemModel = m.itemModel.copy(message = v)))),

  )
}

class FetchItemActionHandler[M](modelRW: ModelRW[M, Pot[Item]]) extends ActionHandler(modelRW) {

  import ItemActionHandler._
  implicit val runner = new RunAfterJS
  import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  override protected def handle = {
    case action: FetchItemAction =>
      val updateF = action.effect(AjaxClient[Api].getItem(action.id.getOrElse("None")).call())(identity _ )
      action.handleWith(this, updateF)(PotAction.handler())
  }
}

class FetchItemListActionHandler[M](modelRW: ModelRW[M, Pot[List[Item]]]) extends ActionHandler(modelRW) {
  import ItemActionHandler._
  implicit val runner = new RunAfterJS
  import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  override protected def handle: PartialFunction[Any, ActionResult[M]] = {
    case action: FetchItemListAction =>
      val updateF = action.effect(AjaxClient[Api].getItemList().call())(identity _ )
      action.handleWith(this, updateF)(PotAction.handler())
  }
}


class ItemActionHandler[M](modelRW: ModelRW[M, Pot[String]]) extends ActionHandler(modelRW) {
  import ItemActionHandler._
  implicit val runner = new RunAfterJS
  import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  override protected def handle = {
    case action: NewItemAction =>
      val updateF = action.effect(AjaxClient[Api].newItem().call())(identity _ )
      action.handleWith(this, updateF)(PotAction.handler())

  }
}
