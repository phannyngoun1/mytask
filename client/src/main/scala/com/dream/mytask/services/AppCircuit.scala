package com.dream.mytask.services

import autowire._
import diode.{Circuit, _}
import diode.data._
import diode.react.ReactConnector
import diode.util._
import boopickle.Default._
import com.dream.mytask.modules.processinst.ProcessInstActionHandler
import com.dream.mytask.modules.task.TaskActionListHandler
import com.dream.mytask.services.DataModel.RootModel
import com.dream.mytask.shared.Api

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

case class FetchAccAction(potResult: Pot[String] = Empty) extends PotAction[String, FetchAccAction] {
  override def next(newResult: Pot[String]): FetchAccAction = FetchAccAction(newResult)
}

case class MessageAction(potResult: Pot[String] = Empty) extends PotAction[String, MessageAction] {
  override def next(newResult: Pot[String]): MessageAction = MessageAction(newResult)
}

class MessageHandler[M](modelRW: ModelRW[M, Pot[String]]) extends ActionHandler(modelRW) {

  implicit val runner = new RunAfterJS

  override protected def handle = {
    case action: MessageAction =>


      val updateF = action.effect(AjaxClient[Api].welcomeMessage("User X").call())(identity _)
      action.handleWith(this, updateF)(PotAction.handler())

    case action: FetchAccAction =>   val updateF = action.effect(AjaxClient[Api].getUser("8dbd6bf8-2f60-4e6e-8e3f-b374e060a940").call())(identity _)
      action.handleWith(this, updateF)(PotAction.handler())
  }
}

object AppCircuit extends Circuit[RootModel] with ReactConnector[RootModel] {

  override protected def initialModel: RootModel = RootModel(
    message = Empty
  )

  override protected def actionHandler: AppCircuit.HandlerFunction = composeHandlers(
    new MessageHandler(zoomRW(_.message)((m, v) => m.copy(message = v))),
    new TaskActionListHandler(zoomRW(_.taskModel.taskList)((m, v) => m.copy(taskModel = m.taskModel.copy(taskList = v)))),
    new ProcessInstActionHandler(zoomRW(_.message)((m, v) => m.copy(message = v)))
  )

}