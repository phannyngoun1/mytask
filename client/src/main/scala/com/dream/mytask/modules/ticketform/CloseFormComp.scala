package com.dream.mytask.modules.ticketform

import com.dream.mytask.AppClient.{Loc}
import com.dream.mytask.modules.form.FormActionHandler.FormAction
import com.dream.mytask.services.DataModel.FormModel
import com.dream.mytask.shared.data.ActionInfoJson
import com.dream.mytask.shared.data.WorkflowData.{TicketStatusPayloadJs}
import diode.react._
import diode.react.ReactPot._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react._

object CloseFormComp {

  case class Props(proxy: ModelProxy[FormModel], c: RouterCtl[Loc], data: ActionInfoJson)

  case class State( status: Option[String] = Some("Cancelled") ,comment: Option[String] = None)

  class Backend($: BackendScope[Props, State]) {
    def render(p: Props, s: State) = {

      val message = p.proxy.connect(_.message)
      <.div(
        "Close form",
        message(px => <.div(
          px().renderPending(_ > 500, _ => <.p("Loading...")),
          px().renderFailed(ex => <.p(s"Failed to load, ${ex}")),
          px().render(m => <.div(^.color := "blue" , m) )
        )),

        <.div(
          <.div(
            <.label("Status:"),
            <.br(),
            <.select(
              ^.value := s.status.getOrElse(""),
              ^.onChange ==> { e: ReactEventFromInput =>
                val value = if (e.target.value.trim.isEmpty) None else Some(e.target.value)
                $.modState(_.copy(status = value))
              },
              <.option( ^.value := "Cancelled", "Cancelled" ),
              <.option( ^.value := "Solved", "Solved"),
            )
          ),
          <.div(
            <.label("Comment:"),
            <.br(),
            <.textarea(^.cols := 60, ^.rows := 3,
              ^.value := s.comment.getOrElse(""),
              ^.onChange ==> { e: ReactEventFromInput =>
                val value = if (e.target.value.trim.isEmpty) None else Some(e.target.value)
              $.modState(_.copy(comment = value))
            } )
          ),
          <.div(
            <.button("Submit",
              ^.onClick --> p.proxy.dispatchCB(FormAction(
                pInstId       = Some(p.data.pInstId.toString),
                taskId        = Some(p.data.taskId.toString),
                accId         = Some(p.data.accountId.toString),
                participantId = Some(p.data.participantId.toString),
                action        = Some(p.data.action),
                payLoad       = Some(TicketStatusPayloadJs(p.data.payloadCode, s.status.get, s.comment))
              ))),
            <.button("Cancel")
          )
        )
      )
    }
  }

  private val component = ScalaComponent.builder[Props]("CloseFormComp")
    .initialStateFromProps(p => State())
    .renderBackend[Backend]
    .build

  def apply(proxy: ModelProxy[FormModel], c: RouterCtl[Loc], data: ActionInfoJson) = component(Props(proxy, c, data))

}
