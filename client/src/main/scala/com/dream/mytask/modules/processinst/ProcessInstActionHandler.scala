package com.dream.mytask.modules.processinst

import autowire._
import com.dream.mytask.services.AjaxClient
import com.dream.mytask.services.DataModel.{ProcessInstanceCriteria, ProcessInstanceModel, ProcessInstanceResult, RootModel}
import com.dream.mytask.shared.Api
import diode.data.{Pot, _}
import diode.util._
import boopickle.Default._
import com.dream.mytask.shared.data.ProcessInstanceData.{PInstInitDataJson, ProcessInstanceJson}
import diode._


object ProcessInstActionHandler {


  case class CreateProcessInstAction(itemId: Option[String], participantId: Option[String], potResult: Pot[String] = Empty) extends PotAction[String, CreateProcessInstAction] {
    override def next(newResult: Pot[String]): CreateProcessInstAction = CreateProcessInstAction(None, None, newResult)
  }

  case class SetPInstIdAction(id: String) extends Action

  case class InitPInstAction(potResult: Pot[PInstInitDataJson] = Empty)  extends PotAction[PInstInitDataJson, InitPInstAction] {
    override def next(newResult: Pot[PInstInitDataJson]): InitPInstAction =  InitPInstAction(newResult)
  }

  case class FetchPInstAction(id: Option[String],potResult: Pot[ProcessInstanceResult] = Empty) extends PotAction[ProcessInstanceResult, FetchPInstAction] {
    override def next(newResult: Pot[ProcessInstanceResult]): FetchPInstAction =  FetchPInstAction(None,newResult)
  }


  case class FetchPInstListAction(potResult: Pot[List[ProcessInstanceJson]] = Empty) extends  PotAction[List[ProcessInstanceJson], FetchPInstListAction] {
    override def next(newResult: Pot[List[ProcessInstanceJson]]): FetchPInstListAction = FetchPInstListAction(newResult)
  }


  def apply(circuit: Circuit[RootModel]) = circuit.composeHandlers(
    new ProcessInstActionHandler(circuit.zoomRW(_.processInst)((m, v) => m.copy(processInst = v))),
    new NewPInstActonHandler(circuit.zoomRW(_.message)((m, v) => m.copy(message = v))),
    new FetchPInstActionHandler(circuit.zoomRW(_.processInst.data)((m, v) => m.copy(processInst = m.processInst.copy(data = v)))),
    new FetchPInstListActionHandler(circuit.zoomRW(_.processInst.list)((m, v) => m.copy(processInst = m.processInst.copy(list = v)))),
    new InitPInstActionHandler(circuit.zoomRW(_.processInst.initData)((m, v) =>  m.copy(processInst = m.processInst.copy(initData = v))))
  )

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

class FetchPInstListActionHandler[M](modelRW: ModelRW[M, Pot[List[ProcessInstanceJson]]]) extends ActionHandler(modelRW) {
  implicit val runner = new RunAfterJS
  import com.dream.mytask.modules.processinst.ProcessInstActionHandler._
  import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  override protected def handle = {
    case action: FetchPInstListAction =>
      val updateF = action.effect(AjaxClient[Api].getPInstanceList().call())(identity _)
      action.handleWith(this, updateF)(PotAction.handler())
  }
}

class NewPInstActonHandler[M](modelRW: ModelRW[M, Pot[String]]) extends ActionHandler(modelRW) {
  implicit val runner = new RunAfterJS
  import com.dream.mytask.modules.processinst.ProcessInstActionHandler._
  import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  override protected def handle = {
    case action: CreateProcessInstAction =>
      val updateF = action.effect(AjaxClient[Api].createProcessInstance(action.itemId.get, action.participantId.get).call())(identity _)
      action.handleWith(this, updateF)(PotAction.handler())
  }
}

class InitPInstActionHandler[M](modelRW: ModelRW[M, Pot[PInstInitDataJson]]) extends ActionHandler(modelRW) {

  implicit val runner = new RunAfterJS
  import com.dream.mytask.modules.processinst.ProcessInstActionHandler._
  import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  override protected def handle = {
    case action: InitPInstAction =>
      val updateF = action.effect(AjaxClient[Api].getPInstInitDat().call())(identity _)
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


