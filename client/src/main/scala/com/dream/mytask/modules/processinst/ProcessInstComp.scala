package com.dream.mytask.modules.processinst

import com.dream.mytask.AppClient.Loc
import com.dream.mytask.modules.processinst.ProcessInstActionHandler.CreateProcessInstAction
import diode.data.Pot
import diode.react.{ModelProxy, ReactConnectProxy}
import japgolly.scalajs.react.BackendScope
import diode.react._
import diode.react.ReactPot._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react._


object ProcessInstComp {

  case class Props(proxy: ModelProxy[Pot[String]], c: RouterCtl[Loc])
  case class State()

  class Backend($: BackendScope[Props, State]) {
    def render(p: Props, s: State) = {

      val wrapper = p.proxy.connect(m => m)
      wrapper(proxy => {

        <.div("Hello",
          <.div(
            proxy().renderPending(_ > 500, _ => <.p("Loading...")),
            proxy().renderFailed(ex => <.p("Failed to load")),
            proxy().render(m => <.p( m))
          ),
          <.div(
            <.button(^.onClick --> p.proxy.dispatchCB(CreateProcessInstAction()), "Create")
          )
        )
      })
    }
  }

  private val  component = ScalaComponent.builder[Props]("DashboardModule")
    .initialStateFromProps(p => State())
    .renderBackend[Backend]
    .build

  def apply(proxy: ModelProxy[Pot[String]], c: RouterCtl[Loc]) = component(Props(proxy, c))
}
