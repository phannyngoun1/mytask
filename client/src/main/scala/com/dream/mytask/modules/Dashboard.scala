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
          <.li(<.a("Account", ^.src := "#", ^.onClick --> p.c.set(AccLoc))),
          <.li(<.a("item", ^.src := "#", ^.onClick --> p.c.set(ItemLoc))),
          <.li(<.a("flow", ^.src := "#",^.onClick --> p.c.set(FlowLoc))),
          <.li(<.a("instance", ^.src := "#",^.onClick --> p.c.set(ProcessInstLoc))),
          <.li(<.a("tasks", ^.src := "#",^.onClick --> p.c.set(TaskListLoc)))
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
