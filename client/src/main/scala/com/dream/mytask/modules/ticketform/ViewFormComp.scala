package com.dream.mytask.modules.ticketform

import com.dream.mytask.AppClient.{FetchTaskLoc, Loc}
import com.dream.mytask.services.DataModel.FormModel
import com.dream.mytask.shared.data.ActionInfoJson
import diode.react._
import diode.react.ReactPot._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react._

object ViewFormComp {

  case class Props(proxy: ModelProxy[FormModel], c: RouterCtl[Loc], data: ActionInfoJson)

  case class State()

  class Backend($: BackendScope[Props, State]) {

    def render(p: Props, s: State) = {
      <.div(
        "View Ticker data form",
        <.div(
          <.label("Comment:"),
          <.br(),
          <.textarea(^.cols := 60, ^.rows := 3)
        ),
        <.div(
          <.button("Submit"),
          <.button("Cancel")
        )
      )
    }
  }

  private val component = ScalaComponent.builder[Props]("TicketViewFormComp")
    .initialStateFromProps(p => State())
    .renderBackend[Backend]
    .build

  def apply(proxy: ModelProxy[FormModel], c: RouterCtl[Loc], data: ActionInfoJson) = component(Props(proxy, c, data))

}
