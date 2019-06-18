package com.dream.mytask.modules.form

import java.util.UUID

import diode._
import diode.data.{Empty, Pot, PotAction}
import diode.util.RunAfterJS
import boopickle.Default._
import autowire._
import com.dream.mytask.modules.form.FormActionHandler.{FetchPInstDataInfoAction, FormAction, PerformTaskAction, StartInstAction}
import com.dream.mytask.services.AjaxClient
import com.dream.mytask.services.DataModel.{FormModel, RootModel}
import com.dream.mytask.shared.Api
import com.dream.mytask.shared.data.{ActionInfoJson, ActionStartInfoJson}
import com.dream.mytask.shared.data.ProcessInstanceData.PInstInitDataInfoJs
import com.dream.mytask.shared.data.WorkflowData.PayloadJs

object FormActionHandler {

  case class FetchPInstDataInfoAction(

    pInstId: Option[UUID],
    taskId: Option[UUID],
    accId: Option[UUID],
    participantId: Option[UUID],
    potResult: Pot[PInstInitDataInfoJs] = Empty

  ) extends PotAction[PInstInitDataInfoJs, FetchPInstDataInfoAction] {

    override def next(newResult: Pot[PInstInitDataInfoJs]) = FetchPInstDataInfoAction(None, None, None, None, newResult)

  }

  trait BasePerformTaskAction {
    def payloadCode:  Option[String]
    def accountId: UUID
    def participantId: UUID

  }

  case class StartInstAction(
    payloadCode: Option[String],
    itemId: UUID,
    accountId: UUID,
    participantId: UUID
  ) extends Action with BasePerformTaskAction

  case class PerformTaskAction(
    payloadCode:  Option[String],
    activity: String,
    action: String,
    taskId: UUID,
    pInstId: UUID,
    accountId: UUID,
    participantId: UUID
  ) extends Action with BasePerformTaskAction

  case class FormAction(
    pInstId: Option[String],
    taskId: Option[String],
    accId: Option[String],
    participantId: Option[String],
    action: Option[String],
    payLoad: Option[PayloadJs],
    potResult: Pot[String] = Empty
  ) extends PotAction[String, FormAction] {
    override def next(newResult: Pot[String]): FormAction = FormAction(None, None, None, None, None, None, newResult)
  }

  def apply(circuit: Circuit[RootModel]) = circuit.composeHandlers(
    new FromActionHandler(circuit.zoomRW(_.formModel)((m, v) =>  m.copy(formModel = v))),
    new FormActionTakenHandler(circuit.zoomRW(_.formModel.message)((m, v) => m.copy(formModel = m.formModel.copy(message = v)))),
    new FetchPInstDataInfoActionHandler(circuit.zoomRW(_.formModel.ticketModel.pInstDataInfo)((m, v) => m.copy(formModel = m.formModel.copy(ticketModel = m.formModel.ticketModel.copy(pInstDataInfo = v)))))
  )
}

class FromActionHandler[M](modelRW: ModelRW[M, FormModel]) extends ActionHandler(modelRW ) {
  implicit val runner = new RunAfterJS

  override protected def handle: PartialFunction[Any, ActionResult[M]] = {
    case PerformTaskAction(payloadCode,activity, action, taskId, pInstId, accountId, participantId) =>
      updated(value.copy(
        message = Pot.empty,
        actionInfo = Some(ActionInfoJson(payloadCode, activity, action, taskId, pInstId, accountId, participantId))
      ))

    case StartInstAction(payloadCode, itemId, accountId, participantId) =>
      updated(value.copy(
        message = Pot.empty,
        actionInfo = Some(ActionStartInfoJson(payloadCode, itemId,accountId, participantId))
      ))
  }
}

class FormActionTakenHandler[M](modelRW: ModelRW[M, Pot[String]]) extends ActionHandler(modelRW) {
  implicit val runner = new RunAfterJS
  import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  override protected def handle: PartialFunction[Any, ActionResult[M]] = {
    case action: FormAction =>
      val updateF = action.effect(AjaxClient[Api].takeAction(action.pInstId.get, action.taskId.get, action.accId.get, action.participantId.get, action.action.get, action.payLoad.get).call())(identity _)
      action.handleWith(this, updateF)(PotAction.handler())
  }
}

class FetchPInstDataInfoActionHandler[M](modelRW: ModelRW[M, Pot[PInstInitDataInfoJs]]) extends ActionHandler(modelRW) {
  implicit val runner = new RunAfterJS
  import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  override protected def handle = {
    case action: FetchPInstDataInfoAction =>
      val updateF = action.effect(AjaxClient[Api].getPInstDetail(action.pInstId.get, action.taskId.get, action.accId.get, action.participantId.get).call())(identity _)
      action.handleWith(this, updateF)(PotAction.handler())
  }
}
