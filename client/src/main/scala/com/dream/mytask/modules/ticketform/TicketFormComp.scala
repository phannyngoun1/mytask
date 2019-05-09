package com.dream.mytask.modules.ticketform

import com.dream.mytask.AppClient.{FetchTaskLoc, Loc}
import diode.react._
import diode.react.ReactPot._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react._

object TicketFormComp {
  case class Props()

  case class State()

  class Backend($: BackendScope[Props, State]) {
    def render(p: Props, s: State) = {
      <.div(
        "Ticker data form"
      )
    }
  }

  private val component = ScalaComponent.builder[Props]("TicketFormComp")
    .initialStateFromProps(p => State())
    .renderBackend[Backend]
    .build

  def apply() = component(Props())
}
