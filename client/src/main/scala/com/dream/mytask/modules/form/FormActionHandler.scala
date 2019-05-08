package com.dream.mytask.modules.form

import java.util.UUID

import diode._
import diode.data.{Empty, Pot, PotAction}
import diode.util.RunAfterJS
import boopickle.Default._
import autowire._
import com.dream.mytask.services.AjaxClient
import com.dream.mytask.services.AppCircuit.zoomRW
import com.dream.mytask.services.DataModel.{FormModel, RootModel}
import com.dream.mytask.shared.Api
import com.dream.mytask.shared.data.ActionInfoJson

object FormActionHandler {

  case class PerformTaskAction(
    activity: String,
    action: String,
    taskId: UUID,
    pInstId: UUID,
    accountId: UUID,
    participantId: UUID
  ) extends Action

  def apply(circuit: Circuit[RootModel]) = circuit.composeHandlers(
    new FromActionHandler(circuit.zoomRW(_.formModel)((m, v) =>  m.copy(formModel = v)))
  )

  class FromActionHandler[M](modelRW: ModelRW[M, FormModel]) extends ActionHandler(modelRW ) {
    implicit val runner = new RunAfterJS

    override protected def handle: PartialFunction[Any, ActionResult[M]] = {
      case PerformTaskAction(activity, action, taskId, pInstId, accountId, participantId) =>
        updated(value.copy(actionInfo = Some(ActionInfoJson(activity, action, taskId, pInstId, accountId, participantId))))
    }
  }

}
