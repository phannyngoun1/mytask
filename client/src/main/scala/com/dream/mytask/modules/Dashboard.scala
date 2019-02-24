package com.dream.mytask.modules

import com.dream.mytask.AppClient.Loc
import com.dream.mytask.services.MessageAction
import diode.data.Pot
import diode.react._
import diode.react.ReactPot._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react._

object Dashboard {

  case class Props(proxy: ModelProxy[Pot[String]], c: RouterCtl[Loc])

  case class State(wrapper: ReactConnectProxy[Pot[String]], searchVal: Option[String] = None)

  class Backend($: BackendScope[Props, State]) {
    def render(p: Props, s: State) = {
      <.div("Hello",
        <.div(
          p.proxy().renderPending(_ > 500, _ => <.p("Loading...")),
          p.proxy().renderFailed(ex => <.p("Failed to load")),
          p.proxy().render(m => <.p(m)),
        )
      )
    }
  }

  private val component = ScalaComponent.builder[Props]("DashboardModule")
    .initialStateFromProps(p => State(p.proxy.connect(m => m)))
    .renderBackend[Backend]
    .componentDidMount(scope =>
      // update only if Motd is empty
      scope.props.proxy.dispatchCB(MessageAction())
    )
    .build

  def apply(proxy: ModelProxy[Pot[String]], c: RouterCtl[Loc]) = component(Props(proxy, c))

}
