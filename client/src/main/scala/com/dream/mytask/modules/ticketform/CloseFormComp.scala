package com.dream.mytask.modules.ticketform

import com.dream.mytask.AppClient.{FetchTaskLoc, Loc}
import diode.react._
import diode.react.ReactPot._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react._

object CloseFormComp {

  case class Props()

  case class State()

  class Backend($: BackendScope[Props, State]) {
    def render(p: Props, s: State) = {
      <.div(
        "Close form",
        <.div(
          <.div(
            <.label("Status:"),
            <.br(),
            <.select(
              <.option( ^.value := 0, "Cancelled"),
              <.option( ^.value := 1, "Solved"),
            )
          ),
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

      )
    }
  }

  private val component = ScalaComponent.builder[Props]("CloseFormComp")
    .initialStateFromProps(p => State())
    .renderBackend[Backend]
    .build

  def apply() = component(Props())

}
