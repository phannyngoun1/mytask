package com.dream.mytask.modules.ticketform

import com.dream.mytask.AppClient.{FetchTaskLoc, Loc}
import diode.react._
import diode.react.ReactPot._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react._

object TicketMainFormComp {

  case class Props(action: String)

  case class State()

  class Backend($: BackendScope[Props, State]) {
    def render(p: Props, s: State) = {
      <.div(
        <.h3("Ticket Form"),
        p.action match {
          case  "Assign" =>
            AssignFormComp()
          case "Close" =>
            CloseFormComp()
          case "Edit" =>
            TicketFormComp()
          case v: String => <.div(s"No action form available form ${v}")
        }
      )
    }
  }

  private val component = ScalaComponent.builder[Props]("TicketFormComp")
    .initialStateFromProps(_ => State())
    .renderBackend[Backend]
    .build

  def apply(action: String) = component(Props(action))



}
