package com.dream.mytask.modules.task

import com.dream.mytask.AppClient.Loc
import com.dream.mytask.modules.task.TaskActionListHandler.TaskListActions.FetchTaskListAction
import com.dream.mytask.shared.data.TaskItem
import diode.data.Pot
import diode.react._
import japgolly.scalajs.react.BackendScope
import diode.react.ReactPot._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react._

object TaskListComp {

  case class Props(proxy: ModelProxy[Pot[List[TaskItem]]], c: RouterCtl[Loc])

  case class State(searchVal: Option[String] = None)

  class Backend($: BackendScope[Props, State]) {
    def render(p: Props, s: State) = {




      val wrapper = p.proxy.connect(m => m)
      wrapper(proxy => {

        <.div("Hello",
          <.div(
            proxy().renderPending(_ > 500, _ => <.p("Loading...")),
            proxy().renderFailed(ex => <.p("Failed to load")),
            proxy().render(m => <.p(m.map(d => s"id = ${d.id}, process instance ID = ${d.id}").mkString(",")))
          )
        )
      })
    }
  }

  private val component = ScalaComponent.builder[Props]("DashboardModule")
    .initialStateFromProps(p => State())
    .renderBackend[Backend]
    .componentDidMount(scope =>
      scope.props.proxy.dispatchCB(FetchTaskListAction())
    )
    .build

  def apply(proxy: ModelProxy[Pot[List[TaskItem]]], c: RouterCtl[Loc]) = component(Props(proxy, c))

}
