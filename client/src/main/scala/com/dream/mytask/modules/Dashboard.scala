package com.dream.mytask.modules

import com.dream.mytask.AppClient.{AccLoc, FlowLoc, ItemLoc, Loc, ProcessInstLoc, TaskListLoc}
import com.dream.mytask.services.{FetchAccAction, MessageAction}
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
      <.div("Menu",
        <.ul(
          <.li(<.a("Account", ^.href := "#", p.c.setOnClick(AccLoc))),
          <.li(<.a("item", ^.href  := "#", p.c.setOnClick(ItemLoc))),
          <.li(<.a("flow", ^.href  := "#", p.c.setOnClick(FlowLoc))),
          <.li(<.a("instance", ^.href := "#",  p.c.setOnClick(ProcessInstLoc)))
        )
      )
    }
  }

  private val component = ScalaComponent.builder[Props]("DashboardModule")
    .initialStateFromProps(p => State(p.proxy.connect(m => m)))
    .renderBackend[Backend]
    .componentDidMount(scope =>
      scope.props.proxy.dispatchCB(MessageAction())
    )
    .build

  def apply(proxy: ModelProxy[Pot[String]], c: RouterCtl[Loc]) = component(Props(proxy, c))

}
