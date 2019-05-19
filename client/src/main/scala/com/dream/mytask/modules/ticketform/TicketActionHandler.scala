package com.dream.mytask.modules.ticketform

import diode._
import diode.data.{Empty, Pot, PotAction}
import diode.util.RunAfterJS
import boopickle.Default._
import autowire._
import com.dream.mytask.modules.ticketform.TicketActionHandler.InitAssignFormAction
import com.dream.mytask.services.AjaxClient
import com.dream.mytask.services.DataModel.RootModel
import com.dream.mytask.shared.Api
import com.dream.mytask.shared.data.AssignFormInitDataJs

object TicketActionHandler {

  case class InitAssignFormAction(potResult: Pot[AssignFormInitDataJs] = Empty)  extends PotAction[AssignFormInitDataJs, InitAssignFormAction] {
    override def next(newResult: Pot[AssignFormInitDataJs]): InitAssignFormAction = InitAssignFormAction(newResult)
  }
  def apply(circuit: Circuit[RootModel]) = circuit.composeHandlers(
    new InitAssignFormActionHandler(circuit.zoomRW(_.formModel.ticketModel.assignFormInitData)((m, v) => m.copy(formModel = m.formModel.copy(ticketModel = m.formModel.ticketModel.copy(assignFormInitData = v)))))
  )
}

class InitAssignFormActionHandler[M](modelRW: ModelRW[M, Pot[AssignFormInitDataJs]]) extends ActionHandler(modelRW) {
  implicit val runner = new RunAfterJS

  import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  override protected def handle  = {
    case action: InitAssignFormAction =>
      val updateF = action.effect(AjaxClient[Api].getTicketAssignInitData().call())(identity _)
      action.handleWith(this, updateF)(PotAction.handler())
  }
}
