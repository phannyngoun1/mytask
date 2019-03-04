package com.dream.mytask.modules.processinst

import autowire._
import com.dream.mytask.services.AjaxClient
import com.dream.mytask.shared.Api
import diode.data.{Pot, _}
import diode.util._
import boopickle.Default._
import diode.{ActionHandler, ModelRW}

object ProcessInstActionHandler {


  case class CreateProcessInstAction(potResult: Pot[String] = Empty) extends PotAction[String, CreateProcessInstAction] {
    override def next(newResult: Pot[String]): CreateProcessInstAction = CreateProcessInstAction(newResult)
  }

}

class ProcessInstActionHandler[M](modelRW: ModelRW[M, Pot[String]]) extends ActionHandler(modelRW) {

  implicit val runner = new RunAfterJS

  import ProcessInstActionHandler._

  import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  override protected def handle = {
    case action: CreateProcessInstAction =>
      val updateF = action.effect(AjaxClient[Api].createProcessInstance().call())(identity _)
      action.handleWith(this, updateF)(PotAction.handler())
  }
}
