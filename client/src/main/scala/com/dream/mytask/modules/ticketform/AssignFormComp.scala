package com.dream.mytask.modules.ticketform

import java.util.UUID

import com.dream.mytask.AppClient.{FetchTaskLoc, Loc}
import com.dream.mytask.modules.form.FormActionHandler.FormAction
import com.dream.mytask.modules.ticketform.TicketActionHandler.InitAssignFormAction
import com.dream.mytask.services.DataModel.FormModel
import com.dream.mytask.shared.data.ActionInfoJson
import com.dream.mytask.shared.data.WorkflowData.{AssignTicketPayloadJs, CommentPayloadJs}
import diode.react._
import diode.react.ReactPot._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react._

object AssignFormComp {

  case class Props(proxy: ModelProxy[FormModel], c: RouterCtl[Loc], data: ActionInfoJson)

  case class State()

  class Backend($: BackendScope[Props, State]) {
    def render(p: Props, s: State) = {

      val message = p.proxy.connect(_.message)
      val initData = p.proxy.connect(_.ticketModel.assignFormInitData)

      <.div(
        "Assign form",

        message(px => <.div(
          px().renderPending(_ > 500, _ => <.p("Loading...")),
          px().renderFailed(ex => <.p(s"Failed to load, ${ex}")),
          px().render(m => <.div(^.color := "blue" , m) )
        )),

        initData(px => <.div(
          px().renderPending(_ > 500, _ => <.p("Loading...")),
          px().renderFailed(ex => <.p(s"Failed to load, ${ex}")),
          px().render { m =>
            <.div(AssignFormDataComp(p.proxy, p.c, m, p.data))
          }
        ))
      )
    }
  }

  private val component = ScalaComponent.builder[Props]("AssignFormComp")
    .initialStateFromProps(p => State())
    .renderBackend[Backend]
    .componentDidMount(f => f.props.proxy.dispatchCB(InitAssignFormAction()))
    .build

  def apply(proxy: ModelProxy[FormModel], c: RouterCtl[Loc], data: ActionInfoJson) = component(Props(proxy, c, data))

}
