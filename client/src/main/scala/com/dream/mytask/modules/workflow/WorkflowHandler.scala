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

  case class FetchFlowAction( id: Option[String], potResult: Pot[FlowJson] = Empty) extends PotAction[FlowJson, FetchFlowAction] {
    override def next(newResult: Pot[FlowJson]): FetchFlowAction = FetchFlowAction(None, newResult)
  }

  def apply(circuit: Circuit[RootModel]) = circuit.composeHandlers(
    new FetchFlowActionHandler(zoomRW(_.flowModel.flow)((m, v) => m.copy(flowModel = m.flowModel.copy(flow= v)))),

  )
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
