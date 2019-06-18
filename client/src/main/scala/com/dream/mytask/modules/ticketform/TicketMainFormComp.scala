package com.dream.mytask.modules.ticketform

import com.dream.mytask.AppClient.{FetchTaskLoc, Loc}
import com.dream.mytask.services.DataModel.FormModel
import com.dream.mytask.shared.data.{ActionInfoJson, BaseActionInfoJson}
import diode.react._
import diode.react.ReactPot._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react._

object TicketMainFormComp {

  case class Props(proxy: ModelProxy[FormModel], c: RouterCtl[Loc], data: BaseActionInfoJson)

  case class State()

  class Backend($: BackendScope[Props, State]) {
    def render(p: Props, s: State) = {
      <.div(
        <.h3("Ticket Form"),
        p.data.payloadCode match {
          case Some("ticket-payload-close") =>
            CloseFormComp(p.proxy, p.c, p.data.asInstanceOf[ActionInfoJson])
          case Some("ticket-payload") =>
            TicketFormComp(p.proxy, p.c, p.data)
          case Some("ticket-payload-view") =>
            ViewFormComp(p.proxy, p.c, p.data.asInstanceOf[ActionInfoJson])
          case Some(v) => <.div(s"No action form available form ${v}")
          case _ => <.div()
        }
      )
    }
  }


  private val component = ScalaComponent.builder[Props]("TicketFormComp")
    .initialStateFromProps(_ => State())
    .renderBackend[Backend]
    .build

  def apply(proxy: ModelProxy[FormModel], c: RouterCtl[Loc], data: BaseActionInfoJson) = component(Props(proxy, c, data))

}
