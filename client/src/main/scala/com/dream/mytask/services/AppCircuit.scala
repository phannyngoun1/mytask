package com.dream.mytask.services

import autowire._
import diode.{Circuit, _}
import diode.data._
import diode.react.ReactConnector
import diode.util._
import boopickle.Default._
import com.dream.mytask.services.DataModel.RootModel
import com.dream.mytask.shared.Api

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

case class MessageAction(potResult: Pot[String] = Empty) extends PotAction[String, MessageAction] {
  override def next(newResult: Pot[String]): MessageAction = MessageAction(newResult)
}

class MessageHandler[M](modelRW: ModelRW[M, Pot[String]]) extends ActionHandler(modelRW) {

  implicit val runner = new RunAfterJS

  override protected def handle = {
    case action: MessageAction =>
      val updateF = action.effect(AjaxClient[Api].welcomeMessage("User X").call())(identity _)
      action.handleWith(this, updateF)(PotAction.handler())
  }
}

object AppCircuit extends Circuit[RootModel] with ReactConnector[RootModel] {

  override protected def initialModel: RootModel = RootModel(
    message = Empty
  )

  override protected def actionHandler: AppCircuit.HandlerFunction = composeHandlers(
    new MessageHandler(zoomRW(_.message)((m, v) => m.copy(message = v))),
  )

}