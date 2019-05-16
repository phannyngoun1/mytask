package com.dream.mytask.modules.ticketform

import com.dream.mytask.AppClient.{FetchTaskLoc, Loc}
import com.dream.mytask.services.DataModel.FormModel
import com.dream.mytask.shared.data.ActionInfoJson
import diode.react._
import diode.react.ReactPot._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react._

object TicketMainFormComp {

  case class Props(proxy: ModelProxy[FormModel], c: RouterCtl[Loc], data: ActionInfoJson)

  case class State()

  class Backend($: BackendScope[Props, State]) {
    def render(p: Props, s: State) = {
      <.div(
        <.h3("Ticket Form"),
        p.data.action match {
          case  "Assign" =>
            AssignFormComp()
          case "Close" =>
            CloseFormComp(p.proxy, p.c, p.data)
          case "Edit" =>
            TicketFormComp()
          case "Comment" =>
            CommentComp()
          case "View" =>
            ViewFormComp(p.proxy, p.c, p.data)
          case v: String => <.div(s"No action form available form ${v}")
        }
      )
    }
  }

  private val component = ScalaComponent.builder[Props]("TicketFormComp")
    .initialStateFromProps(_ => State())
    .renderBackend[Backend]
    .build

  def apply(proxy: ModelProxy[FormModel], c: RouterCtl[Loc], data: ActionInfoJson) = component(Props(proxy, c, data))

}
