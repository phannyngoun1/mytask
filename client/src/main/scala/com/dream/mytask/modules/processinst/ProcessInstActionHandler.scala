package com.dream.mytask.modules.processinst

import autowire._
import com.dream.mytask.services.AjaxClient
import com.dream.mytask.services.DataModel.{ProcessInstanceCriteria, ProcessInstanceModel, ProcessInstanceResult}
import com.dream.mytask.shared.Api
import diode.data.{Pot, _}
import diode.util._
import boopickle.Default._
import diode.{Action, ActionHandler, ActionResult, ModelRW}

object ProcessInstActionHandler {


  case class CreateProcessInstAction(potResult: Pot[String] = Empty) extends PotAction[String, CreateProcessInstAction] {
    override def next(newResult: Pot[String]): CreateProcessInstAction = CreateProcessInstAction(newResult)
  }

  case class SetPInstIdAction(id: String) extends Action

  case class FetchPInstAction(id: Option[String],potResult: Pot[ProcessInstanceResult] = Empty) extends PotAction[ProcessInstanceResult, FetchPInstAction] {
    override def next(newResult: Pot[ProcessInstanceResult]): FetchPInstAction =  FetchPInstAction(None,newResult)
  }

}

class FetchPInstActionHandler[M](modelRW: ModelRW[M, Pot[ProcessInstanceResult]]) extends ActionHandler(modelRW) {
  implicit val runner = new RunAfterJS
  import com.dream.mytask.modules.processinst.ProcessInstActionHandler._
  import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
  override protected def handle = {
    case action: FetchPInstAction =>
      val updateF = action.effect(AjaxClient[Api].getProcessInstance(action.id.getOrElse("None")).call())(m => ProcessInstanceResult(m))
      action.handleWith(this, updateF)(PotAction.handler())
  }
}

class NewPInstActonHandler[M](modelRW: ModelRW[M, Pot[String]]) extends ActionHandler(modelRW) {
  implicit val runner = new RunAfterJS
  import com.dream.mytask.modules.processinst.ProcessInstActionHandler._
  import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  override protected def handle = {
    case action: CreateProcessInstAction =>
      val updateF = action.effect(AjaxClient[Api].createProcessInstance().call())(identity _)
      action.handleWith(this, updateF)(PotAction.handler())
  }
}

class ProcessInstActionHandler[M](modelRW: ModelRW[M, ProcessInstanceModel]) extends ActionHandler(modelRW) {

  implicit val runner = new RunAfterJS
  import com.dream.mytask.modules.processinst.ProcessInstActionHandler._

  override protected def handle: PartialFunction[Any, ActionResult[M]] = {
    case SetPInstIdAction(id) => updated(value.copy(criteria = Some(ProcessInstanceCriteria(id))))
  }
}
