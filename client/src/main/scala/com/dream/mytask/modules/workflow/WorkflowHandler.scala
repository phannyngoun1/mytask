package com.dream.mytask.modules.workflow

import diode._
import diode.data.{Empty, Pot, PotAction}
import diode.util.RunAfterJS
import boopickle.Default._
import autowire._
import com.dream.mytask.services.DataModel.RootModel
import com.dream.mytask.shared.data.WorkflowData.FlowJson
import com.dream.mytask.services._
import com.dream.mytask.shared.Api
import com.dream.mytask.services.AppCircuit.zoomRW

object WorkflowHandler {


  case class FetchFlowListAction( potResult: Pot[List[FlowJson]] = Empty) extends PotAction[List[FlowJson], FetchFlowListAction] {
    override def next(newResult: Pot[List[FlowJson]]): FetchFlowListAction = FetchFlowListAction(newResult)
  }

  case class FetchFlowAction( id: Option[String], potResult: Pot[FlowJson] = Empty) extends PotAction[FlowJson, FetchFlowAction] {
    override def next(newResult: Pot[FlowJson]): FetchFlowAction = FetchFlowAction(None, newResult)
  }

  case class NewFlowAction(name: Option[String], participants: Option[List[String]], potResult: Pot[String] = Pot.empty ) extends PotAction[String, NewFlowAction] {
    override def next(newResult: Pot[String]): NewFlowAction = NewFlowAction(name,participants , newResult)
  }

  def apply(circuit: Circuit[RootModel]) = circuit.composeHandlers(
    new FetchFlowListActionHandler(zoomRW(_.flowModel.flowList)((m, v) => m.copy(flowModel = m.flowModel.copy(flowList= v)))),
    new FetchFlowActionHandler(zoomRW(_.flowModel.flow)((m, v) => m.copy(flowModel = m.flowModel.copy(flow= v)))),
    new NewFlowActionHandler(zoomRW(_.flowModel.message)((m, v) => m.copy(flowModel = m.flowModel.copy(message = v)))),

  )
}

class FetchFlowListActionHandler[M](modelRW: ModelRW[M, Pot[List[FlowJson]]]) extends ActionHandler(modelRW) {

  import WorkflowHandler._
  implicit val runner = new RunAfterJS
  import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  override protected def handle: PartialFunction[Any, ActionResult[M]] = {
    case action: FetchFlowListAction =>

      print("gggg")

      val updateF = action.effect(AjaxClient[Api].getFlowList().call())(identity _ )
      action.handleWith(this, updateF)(PotAction.handler())
  }
}

class FetchFlowActionHandler[M](modelRW: ModelRW[M, Pot[FlowJson]]) extends ActionHandler(modelRW) {

  import WorkflowHandler._
  implicit val runner = new RunAfterJS
  import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  override protected def handle = {
    case action: FetchFlowAction =>
      val updateF = action.effect(AjaxClient[Api].getFlow(action.id.getOrElse("None")).call())(identity _ )
      action.handleWith(this, updateF)(PotAction.handler())
  }
}

class NewFlowActionHandler[M](modelRW: ModelRW[M, Pot[String]]) extends ActionHandler(modelRW) {

  import WorkflowHandler._
  implicit val runner = new RunAfterJS
  import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  override protected def handle: PartialFunction[Any, ActionResult[M]] = {
    case action: NewFlowAction =>
      val updateF = action.effect(AjaxClient[Api].newFlow(action.name.getOrElse("None"), action.participants.get).call())(identity _ )
      action.handleWith(this, updateF)(PotAction.handler())
  }


}
