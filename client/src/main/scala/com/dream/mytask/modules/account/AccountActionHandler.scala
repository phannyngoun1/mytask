package com.dream.mytask.modules.account

import java.util.UUID

import diode._
import diode.data.{Empty, Pot, PotAction}
import diode.util.RunAfterJS
import boopickle.Default._
import autowire._
import com.dream.mytask.services.AjaxClient
import com.dream.mytask.services.AppCircuit.zoomRW
import com.dream.mytask.services.DataModel.RootModel
import com.dream.mytask.shared.Api
import com.dream.mytask.shared.data.AccountData.{AccountJson, ParticipantJson}

object AccountActionHandler {

  case class FetchAccListAction(potResult: Pot[List[AccountJson]] = Empty) extends PotAction[List[AccountJson], FetchAccListAction] {
    override def next(newResult: Pot[List[AccountJson]]): FetchAccListAction = FetchAccListAction(newResult)
  }

  case class FetchParticipantListAction(potResult: Pot[List[ParticipantJson]] = Empty) extends PotAction[List[ParticipantJson], FetchParticipantListAction] {
    override def next(newResult: Pot[List[ParticipantJson]]): FetchParticipantListAction = FetchParticipantListAction(newResult)
  }

  case class FetchAccAction( id: Option[String], potResult: Pot[Option[AccountJson]] = Empty) extends PotAction[Option[AccountJson], FetchAccAction] {
    override def next(newResult: Pot[Option[AccountJson]]): FetchAccAction = FetchAccAction(None, newResult)
  }

  case class FetchParticipantAction( id: Option[String], potResult: Pot[Option[ParticipantJson]] = Empty) extends PotAction[Option[ParticipantJson], FetchParticipantAction] {
    override def next(newResult: Pot[Option[ParticipantJson]]): FetchParticipantAction = FetchParticipantAction(None, newResult)
  }

  case class NewAccAction(name: Option[String], desc: Option[String], potResult: Pot[String] = Empty) extends PotAction[String, NewAccAction] {
    override def next(newResult: Pot[String]): NewAccAction = NewAccAction(None, None, newResult)
  }

  case class NewParticipantAction(accountId: Option[String], potResult: Pot[String] = Empty) extends PotAction[String, NewParticipantAction] {
    override def next(newResult: Pot[String]): NewParticipantAction = NewParticipantAction(None, newResult)
  }

  def apply(circuit: Circuit[RootModel]) = circuit.composeHandlers(
    new FetchAccActionHandler(zoomRW(_.accountModel.account)((m, v) => m.copy(accountModel = m.accountModel.copy(account = v)))),
    new FetchAccListActionHandler(zoomRW(_.accountModel.accountList)((m, v) => m.copy(accountModel = m.accountModel.copy(accountList = v)))),
    new NewAccActionHandler(zoomRW(_.accountModel.message)((m, v) => m.copy(accountModel = m.accountModel.copy(message = v)))),
    new FetchParticipantListActionHandler(zoomRW(_.accountModel.participantList)((m, v) => m.copy(accountModel = m.accountModel.copy(participantList = v)))),
    new FetchParticipantActionHandler(zoomRW(_.accountModel.participant)((m, v) => m.copy(accountModel = m.accountModel.copy(participant = v))))
  )

}

class FetchAccActionHandler[M](modelRW: ModelRW[M, Pot[Option[AccountJson]]]) extends ActionHandler(modelRW) {

  import AccountActionHandler._
  implicit val runner = new RunAfterJS
  import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  override protected def handle = {
    case action: FetchAccAction =>
      val updateF = action.effect(AjaxClient[Api].getAcc(action.id.getOrElse("None")).call())(identity _)
      action.handleWith(this, updateF)(PotAction.handler())
  }
}

class FetchParticipantActionHandler[M](modelRW: ModelRW[M, Pot[Option[ParticipantJson]]]) extends ActionHandler(modelRW) {

  import AccountActionHandler._
  implicit val runner = new RunAfterJS
  import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  override protected def handle = {
    case action: FetchParticipantAction=>
      val updateF = action.effect(AjaxClient[Api].getParticipant(action.id.map(UUID.fromString).get).call())(identity _)
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

class FetchParticipantListActionHandler[M](modelRW: ModelRW[M, Pot[List[ParticipantJson]]]) extends ActionHandler(modelRW) {

  import AccountActionHandler._
  implicit val runner = new RunAfterJS
  import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  override protected def handle: PartialFunction[Any, ActionResult[M]] = {
    case action: FetchParticipantListAction=>
      val updateF = action.effect(AjaxClient[Api].getParticipantList().call())(identity _)
      action.handleWith(this, updateF)(PotAction.handler())
  }
}

class NewAccActionHandler[M](modelRW: ModelRW[M, Pot[String]]) extends ActionHandler(modelRW) {

  import AccountActionHandler._
  implicit val runner = new RunAfterJS
  import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  override protected def handle: PartialFunction[Any, ActionResult[M]] = {
    case action: NewAccAction =>
      val updateF = action.effect(AjaxClient[Api].newAccount(action.name.get, action.desc.get).call())(identity _)
      action.handleWith(this, updateF)(PotAction.handler())

    case action: NewParticipantAction =>
      val updateF = action.effect(AjaxClient[Api].newParticipant(action.accountId.map(UUID.fromString).get).call())(identity _)
      action.handleWith(this, updateF)(PotAction.handler())

  }

}
