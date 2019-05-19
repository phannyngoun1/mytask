package com.dream.mytask.modules.ticketform

import com.dream.mytask.AppClient.{FetchTaskLoc, Loc}
import com.dream.mytask.modules.form.FormActionHandler.FormAction
import com.dream.mytask.services.DataModel.FormModel
import com.dream.mytask.shared.data.ActionInfoJson
import com.dream.mytask.shared.data.WorkflowData.{TicketStatusPayloadJs, CommentPayloadJs}
import diode.react._
import diode.react.ReactPot._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react._

object CommentComp {

  case class Props(proxy: ModelProxy[FormModel], c: RouterCtl[Loc], data: ActionInfoJson)

  case class State(comment: Option[String] = None )

  class Backend($: BackendScope[Props, State]) {
    def render(p: Props, s: State) = {

      val message = p.proxy.connect(_.message)

      <.div(
        message(px => <.div(
          px().renderPending(_ > 500, _ => <.p("Loading...")),
          px().renderFailed(ex => <.p(s"Failed to load, ${ex}")),
          px().render(m => <.div(^.color := "blue" , m) )
        )),
        <.div(
          <.label("Comment:"),
          <.br(),
          <.textarea(
            ^.value := s.comment.getOrElse(""),
            ^.onChange ==> { e: ReactEventFromInput =>
              val value = if (e.target.value.trim.isEmpty) None else Some(e.target.value)
              $.modState(_.copy(comment = value))
            },
            ^.cols := 60,
            ^.rows := 3
          )
        ),
        <.div(
          <.button("Submit",
            ^.onClick --> Callback.when(s.comment.isDefined)(p.proxy.dispatchCB(FormAction(
              pInstId       = Some(p.data.pInstId.toString),
              taskId        = Some(p.data.taskId.toString),
              accId         = Some(p.data.accountId.toString),
              participantId = Some(p.data.participantId.toString),
              action        = Some(p.data.action),
              payLoad       = Some(CommentPayloadJs(s.comment))
            )))
          ),
          <.button("Cancel")
        )
      )
    }
  }

  private val component = ScalaComponent.builder[Props]("CommentComp")
    .initialStateFromProps(p => State())
    .renderBackend[Backend]
    .build

  def apply(proxy: ModelProxy[FormModel], c: RouterCtl[Loc], data: ActionInfoJson) = component(Props(proxy, c, data))
}
