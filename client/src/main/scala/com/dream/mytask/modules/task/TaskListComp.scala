package com.dream.mytask.modules.task

import com.dream.mytask.AppClient.Loc
import com.dream.mytask.modules.task.TaskActionListHandler.TaskListActions.{FetchTaskListAction, TakeAction}
import com.dream.mytask.services.DataModel.TaskModel
import com.dream.mytask.shared.data.{ActionItemJson, TaskItemJson}
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

      implicit  def renderItem(task: TaskItemJson, action: ActionItemJson, accId: String) = {

        <.div(
          <.button(action.name, ^.onClick --> p.proxy.dispatchCB(TakeAction(Some(task.pInstId), Some(task.id), Some(accId), Some(task.participantId), Some(action.name))))
        )
      }

      val wrapper = p.proxy.connect(_.taskList)
      val message = p.proxy.connect(_.message)
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

          <.div(
            message(px =>
              <.div(
                px().renderPending(_ > 500, _ => <.p("Loading...")),
                px().renderFailed(ex => <.p("Failed to load")),
                px().render(m => m )
              )
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
                    <.li( s"P inst: ${item.pInstId}, task Id: ${item.id}, participantId: ${item.participantId}, activity: ${item.activityName}",
                      item.actions toTagMod(action => renderItem(item, action, s.accountId.get))
                    )
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
