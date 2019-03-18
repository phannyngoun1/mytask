package com.dream.mytask.modules.account

import diode._
import diode.data.{Empty, Pot, PotAction}
import diode.util.RunAfterJS
import boopickle.Default._
import autowire._
import com.dream.mytask.services.AjaxClient
import com.dream.mytask.services.AppCircuit.zoomRW
import com.dream.mytask.services.DataModel.RootModel
import com.dream.mytask.shared.Api
import com.dream.mytask.shared.data.AccountData.AccountJson

object AccountActionHandler {

  case class FetchAccListAction(potResult: Pot[List[AccountJson]] = Empty) extends PotAction[List[AccountJson], FetchAccListAction] {
    override def next(newResult: Pot[List[AccountJson]]): FetchAccListAction = FetchAccListAction(newResult)
  }

  case class FetchAccAction( id: Option[String], potResult: Pot[AccountJson] = Empty) extends PotAction[AccountJson, FetchAccAction] {
    override def next(newResult: Pot[AccountJson]): FetchAccAction = FetchAccAction(None, newResult)
  }

  case class NewAccAction(name: Option[String], desc: Option[String], potResult: Pot[String] = Empty) extends PotAction[String, NewAccAction] {
    override def next(newResult: Pot[String]): NewAccAction = NewAccAction(None, None, newResult)
  }

  def apply(circuit: Circuit[RootModel]) = circuit.composeHandlers(
  )

}

class FetchAccActionHandler[M](modelRW: ModelRW[M, Pot[AccountJson]]) extends ActionHandler(modelRW) {

  import AccountActionHandler._
  implicit val runner = new RunAfterJS
  import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  override protected def handle = {
    case action: FetchAccAction =>
      val updateF = action.effect(AjaxClient[Api].getAcc(action.id.getOrElse("None")).call())(identity _)
      action.handleWith(this, updateF)(PotAction.handler())
  }
}

class FetchAccListActionHandler[M](modelRW: ModelRW[M, Pot[List[AccountJson]]]) extends ActionHandler(modelRW) {

  import AccountActionHandler._
  implicit val runner = new RunAfterJS
  import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  override protected def handle: PartialFunction[Any, ActionResult[M]] = {
    case action: FetchAccListAction =>
      val updateF = action.effect(AjaxClient[Api].getAccountList().call())(identity _)
      action.handleWith(this, updateF)(PotAction.handler())
  }
}

class NewAccActionHandler[M](modelRW: ModelRW[M, Pot[String]]) extends ActionHandler(modelRW) {

  import AccountActionHandler._
  implicit val runner = new RunAfterJS
  import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  override protected def handle: PartialFunction[Any, ActionResult[M]] ={
    case action: NewAccAction =>
      val updateF = action.effect(AjaxClient[Api].newAccount(action.name.get, action.desc.get).call())(identity _)
      action.handleWith(this, updateF)(PotAction.handler())
  }

}
