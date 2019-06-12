package com.dream.mytask.modules.workflow

import java.util.UUID

import diode._
import diode.data.{Empty, Pot, PotAction}
import diode.util.RunAfterJS
import boopickle.Default._
import autowire._
import com.dream.mytask.services.DataModel.RootModel
import com.dream.mytask.shared.data.WorkflowData.{FlowInitDataJs, FlowJson, WorkflowTemplateJs}
import com.dream.mytask.services._
import com.dream.mytask.shared.Api
import com.dream.mytask.services.AppCircuit.zoomRW

object WorkflowHandler {


  case class InitFlowDataAction( potResult: Pot[FlowInitDataJs] = Empty) extends PotAction[FlowInitDataJs, InitFlowDataAction] {
    override def next(newResult: Pot[FlowInitDataJs]): InitFlowDataAction = InitFlowDataAction(newResult)
  }

  case class FetchFlowListAction( potResult: Pot[List[FlowJson]] = Empty) extends PotAction[List[FlowJson], FetchFlowListAction] {
    override def next(newResult: Pot[List[FlowJson]]): FetchFlowListAction = FetchFlowListAction(newResult)
  }

  case class FetchFlowAction( id: Option[UUID], potResult: Pot[WorkflowTemplateJs] = Empty) extends PotAction[WorkflowTemplateJs, FetchFlowAction] {
    override def next(newResult: Pot[WorkflowTemplateJs]): FetchFlowAction = FetchFlowAction(None, newResult)
  }

  case class FetchWorkflowTemplateAction(id: Option[UUID],potResult: Pot[WorkflowTemplateJs]  = Empty) extends PotAction[WorkflowTemplateJs, FetchWorkflowTemplateAction] {
    override def next(newResult: Pot[WorkflowTemplateJs]): FetchWorkflowTemplateAction = FetchWorkflowTemplateAction(None, newResult)
  }

  case class NewFlowAction(workflow: Option[WorkflowTemplateJs], potResult: Pot[String] = Pot.empty ) extends PotAction[String, NewFlowAction] {
    override def next(newResult: Pot[String]): NewFlowAction = NewFlowAction(workflow, newResult)
  }

  def apply(circuit: Circuit[RootModel]) = circuit.composeHandlers(
    new FetchFlowListActionHandler(zoomRW(_.flowModel.flowList)((m, v) => m.copy(flowModel = m.flowModel.copy(flowList= v)))),
    new FetchFlowActionHandler(zoomRW(_.flowModel.workflow)((m, v) => m.copy(flowModel = m.flowModel.copy(workflow= v)))),
    new NewFlowActionHandler(zoomRW(_.flowModel.message)((m, v) => m.copy(flowModel = m.flowModel.copy(message = v)))),
    new InitFlowDataActionHandler(zoomRW(_.flowModel.initData)((m, v) => m.copy(flowModel = m.flowModel.copy(initData = v)))),
    new FetchWorkflowTemplateActionHandler(zoomRW(_.flowModel.workflowTemplate)((m, v) => m.copy(flowModel = m.flowModel.copy(workflowTemplate= v)))),
  )
}

class InitFlowDataActionHandler[M](modelRW: ModelRW[M, Pot[FlowInitDataJs]]) extends ActionHandler(modelRW) {
  import WorkflowHandler._
  implicit val runner = new RunAfterJS
  import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  override protected def handle: PartialFunction[Any, ActionResult[M]] = {
    case action: InitFlowDataAction =>
      val updateF = action.effect(AjaxClient[Api].getFlowInitData().call())(identity _ )
      action.handleWith(this, updateF)(PotAction.handler())
  }
}

class FetchFlowListActionHandler[M](modelRW: ModelRW[M, Pot[List[FlowJson]]]) extends ActionHandler(modelRW) {

  import WorkflowHandler._
  implicit val runner = new RunAfterJS
  import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  override protected def handle: PartialFunction[Any, ActionResult[M]] = {
    case action: FetchFlowListAction =>
      val updateF = action.effect(AjaxClient[Api].getFlowList().call())(identity _ )
      action.handleWith(this, updateF)(PotAction.handler())
  }
}



class FetchFlowActionHandler[M](modelRW: ModelRW[M, Pot[WorkflowTemplateJs]]) extends ActionHandler(modelRW) {

  import WorkflowHandler._
  implicit val runner = new RunAfterJS
  import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  override protected def handle = {
    case action: FetchFlowAction =>
      val updateF = action.effect(AjaxClient[Api].getFlow(action.id.get).call())(identity _ )
      action.handleWith(this, updateF)(PotAction.handler())
  }
}


class FetchWorkflowTemplateActionHandler[M](modelRW: ModelRW[M, Pot[WorkflowTemplateJs]]) extends ActionHandler(modelRW) {

  import WorkflowHandler._
  implicit val runner = new RunAfterJS
  import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  override protected def handle = {
    case action: FetchWorkflowTemplateAction =>
      val updateF = action.effect(AjaxClient[Api].getFlowTemplate(action.id.get).call())(identity _ )
      action.handleWith(this, updateF)(PotAction.handler())
  }
}

class NewFlowActionHandler[M](modelRW: ModelRW[M, Pot[String]]) extends ActionHandler(modelRW) {

  import WorkflowHandler._
  implicit val runner = new RunAfterJS
  import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  override protected def handle: PartialFunction[Any, ActionResult[M]] = {
    case action: NewFlowAction =>
      val updateF = action.effect(AjaxClient[Api].newFlow(action.workflow.get).call())(identity _ )
      action.handleWith(this, updateF)(PotAction.handler())
  }


}
