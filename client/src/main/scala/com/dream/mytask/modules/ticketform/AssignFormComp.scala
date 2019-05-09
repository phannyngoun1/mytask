package com.dream.mytask.modules.ticketform

import java.util.UUID

import com.dream.mytask.AppClient.{FetchTaskLoc, Loc}
import diode.react._
import diode.react.ReactPot._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react._

object AssignFormComp {

  case class Props()

  case class State()

  class Backend($: BackendScope[Props, State]) {
    def render(p: Props, s: State) = {
      <.div(
        "Assign form"
      )
    }
  }

  private val component = ScalaComponent.builder[Props]("AssignFormComp")
    .initialStateFromProps(p => State())
    .renderBackend[Backend]
    .build

  def apply() = component(Props())

}
