package com.dream.mytask.modules.item

import com.dream.mytask.services._
import com.dream.mytask.shared.Api
import com.dream.mytask.shared.data.ItemData.{ItemInitDataJs, ItemJson}
import diode._
import diode.data.{Empty, Pot, PotAction}
import diode.util.RunAfterJS
import boopickle.Default._
import autowire._
import com.dream.mytask.services.AppCircuit.zoomRW
import com.dream.mytask.services.DataModel._

object ItemActionHandler {


  case class InitItemDataAction(potResult: Pot[ItemInitDataJs] = Empty) extends PotAction[ItemInitDataJs, InitItemDataAction] {
    override def next(newResult: Pot[ItemInitDataJs]): InitItemDataAction = InitItemDataAction(newResult)
  }

  case class FetchItemListAction(potResult: Pot[List[ItemJson]] = Empty) extends PotAction[List[ItemJson], FetchItemListAction] {
    override def next(newResult: Pot[List[ItemJson]]): FetchItemListAction = FetchItemListAction(newResult)
  }

  case class FetchItemAction( id: Option[String], potResult: Pot[ItemJson] = Empty) extends PotAction[ItemJson, FetchItemAction] {
    override def next(newResult: Pot[ItemJson]): FetchItemAction = FetchItemAction(None, newResult)
  }

  case class NewItemAction(name: Option[String], flowId: Option[String], desc: Option[String], potResult: Pot[String] = Empty) extends PotAction[String, NewItemAction]{

    override def next(newResult: Pot[String]): NewItemAction = NewItemAction(None, None, None, newResult)

  }

  def apply(circuit: Circuit[RootModel]) = circuit.composeHandlers(
    new FetchItemListActionHandler(zoomRW(_.itemModel.itemList)((m, v) => m.copy(itemModel = m.itemModel.copy(itemList = v)))),
    new FetchItemActionHandler(zoomRW(_.itemModel.item)((m, v) => m.copy(itemModel = m.itemModel.copy(item = v)))),
    new ItemActionHandler(zoomRW(_.itemModel.message)((m, v) => m.copy(itemModel = m.itemModel.copy(message = v)))),
    new InitItemDataActionHandler(zoomRW(_.itemModel.initData)((m, v) => m.copy(itemModel = m.itemModel.copy(initData = v))))
  )
}

class FetchItemActionHandler[M](modelRW: ModelRW[M, Pot[ItemJson]]) extends ActionHandler(modelRW) {

  import ItemActionHandler._
  implicit val runner = new RunAfterJS
  import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  override protected def handle = {
    case action: FetchItemAction =>
      val updateF = action.effect(AjaxClient[Api].getItem(action.id.getOrElse("None")).call())(identity _ )
      action.handleWith(this, updateF)(PotAction.handler())
  }
}

class FetchItemListActionHandler[M](modelRW: ModelRW[M, Pot[List[ItemJson]]]) extends ActionHandler(modelRW) {
  import ItemActionHandler._
  implicit val runner = new RunAfterJS
  import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  override protected def handle: PartialFunction[Any, ActionResult[M]] = {
    case action: FetchItemListAction =>
      val updateF = action.effect(AjaxClient[Api].getItemList().call())(identity _ )
      action.handleWith(this, updateF)(PotAction.handler())
  }
}

class InitItemDataActionHandler[M](modelRW: ModelRW[M, Pot[ItemInitDataJs]]) extends ActionHandler(modelRW) {
  import ItemActionHandler._
  implicit val runner = new RunAfterJS
  import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  override protected def handle = {
    case action: InitItemDataAction =>
      val updateF = action.effect(AjaxClient[Api].getItemInitData().call())(identity _ )
      action.handleWith(this, updateF)(PotAction.handler())
  }
}

class ItemActionHandler[M](modelRW: ModelRW[M, Pot[String]]) extends ActionHandler(modelRW) {
  import ItemActionHandler._
  implicit val runner = new RunAfterJS
  import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  override protected def handle = {
    case action: NewItemAction =>
      val updateF = action.effect(AjaxClient[Api].newItem(action.name.get, action.flowId.get, action.desc).call())(identity _ )
      action.handleWith(this, updateF)(PotAction.handler())
  }
}
