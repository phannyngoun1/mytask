package com.dream.mytask.modules

import com.dream.mytask.AppClient.Loc
import japgolly.scalajs.react.ScalaComponent
import japgolly.scalajs.react.component.Scala.BackendScope
import japgolly.scalajs.react.extra.router.{Resolution, RouterCtl}
import japgolly.scalajs.react.vdom.html_<^.<

object Main {

  case class Props(c: RouterCtl[Loc], r: Resolution[Loc])
  case class State(st: String)

  class Backend($: BackendScope[Props, State]){

    def render(p: Props, s: State) = {
      <.div(p.r.render())
    }
  }

  private val component = ScalaComponent.builder[Props]("DashboardModule")
    .initialState(State("good"))
    .renderBackend[Backend]
    .build

  def apply(props: Props) = component(props)

}
