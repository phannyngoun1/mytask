package com.dream.mytask.modules.task

import com.dream.mytask.AppClient.Loc
import com.dream.mytask.modules.task.TaskActionListHandler.TaskListActions.FetchTaskListAction
import com.dream.mytask.services.DataModel.TaskModel
import diode.react._
import japgolly.scalajs.react.BackendScope
import diode.react.ReactPot._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react._

object TaskListComp {

  case class Props(proxy: ModelProxy[TaskModel], c: RouterCtl[Loc])

  case class State(searchVal: Option[String] = None, accountId: Option[String] = None)

  class Backend($: BackendScope[Props, State]) {
    def render(p: Props, s: State) = {

      val wrapper = p.proxy.connect(_.taskList)
        <.div(
          <.div(
            <.label("Account Id: "),
            <.input(^.`type` := "text", ^.value := s.accountId.getOrElse(""), ^.onChange ==> { e: ReactEventFromInput =>
              val value = if (e.target.value.trim.isEmpty) None else Some(e.target.value)
              $.modState(_.copy(accountId = value))
            }),
            <.button("Fetch Task", ^.onClick --> Callback.when(s.accountId.isDefined)(
              p.proxy.dispatchCB(FetchTaskListAction(s.accountId)))
            )
          ),

          <.h3(s"Task list for account: ${s.accountId.getOrElse("None")}" ),

          <.div(
            wrapper(px => {
              <.div(
                px().renderPending(_ > 500, _ => <.p("Loading...")),
                px().renderFailed(ex => <.p("Failed to load")),
                px().render(m => <.ol( ^.`type` := "1",
                  m toTagMod { item =>
                    <.li( s"P inst: ${item.pInstId}, task Id: ${item.id}, activity: ${item.activityName}, actions: ${item.actions.mkString(";")}")
                  }
                ))
              )
            })
          )
        )
      }
  }

  private val component = ScalaComponent.builder[Props]("DashboardModule")
    .initialStateFromProps(p => State())
    .renderBackend[Backend]
    .build

  def apply(proxy: ModelProxy[TaskModel], c: RouterCtl[Loc]) = component(Props(proxy, c))

}
