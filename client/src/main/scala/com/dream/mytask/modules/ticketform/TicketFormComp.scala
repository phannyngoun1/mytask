package com.dream.mytask.modules.ticketform

import com.dream.mytask.AppClient.{Loc}
import com.dream.mytask.modules.form.FormActionHandler.FormAction
import com.dream.mytask.modules.processinst.ProcessInstActionHandler.{CreateProcessInstAction}
import com.dream.mytask.services.DataModel.FormModel
import com.dream.mytask.shared.data.{ActionInfoJson, ActionStartInfoJson, BaseActionInfoJson}
import com.dream.mytask.shared.data.WorkflowData.{EditTicketPayloadJs}
import diode.react._
import diode.react.ReactPot._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react._

object TicketFormComp {

  case class Props(proxy: ModelProxy[FormModel], c: RouterCtl[Loc], data: BaseActionInfoJson)

  case class State(test: String = "",comment: Option[String] = None)

  class Backend($: BackendScope[Props, State]) {
    def render(p: Props, s: State) = {

      val message = p.proxy.connect(_.message)

      <.div(
        "Ticker data form",

        message(px => <.div(
          px().renderPending(_ > 500, _ => <.p("Loading...")),
          px().renderFailed(ex => <.p(s"Failed to load, ${ex}")),
          px().render(m => <.div(^.color := "blue" , m) )
        )),

        <.div(
          <.label("Sth:"),
          <.br(),
          <.input(
            ^.value := s.test,
            ^.onChange ==> { e: ReactEventFromInput =>
              val value = if (e.target.value.trim.isEmpty) None else Some(e.target.value)
              $.modState(_.copy(test=value.getOrElse("")))
            },
          ),
          <.br(),
          <.label("Comment:"),
          <.br(),
          <.textarea(
            ^.cols := 60,
            ^.rows := 3,
            ^.value := s.comment.getOrElse(""),
            ^.onChange ==> { e: ReactEventFromInput =>
              val value = if (e.target.value.trim.isEmpty) None else Some(e.target.value)
              $.modState(_.copy(comment = value))
            },
          )
        ),
        <.div(
          <.button("Submit",
            ^.onClick --> {

              p.data match {
                case actionInfo: ActionInfoJson =>
                  p.proxy.dispatchCB(FormAction(
                    pInstId       = Some(actionInfo.pInstId.toString),
                    taskId        = Some(actionInfo.taskId.toString),
                    accId         = Some(actionInfo.accountId.toString),
                    participantId = Some(actionInfo.participantId.toString),
                    action        = Some(actionInfo.action),
                    payLoad       = Some(EditTicketPayloadJs(actionInfo.payloadCode, s.test,s.comment))
                  ))

                case actionInfo: ActionStartInfoJson =>
                  Callback.when(!s.test.isEmpty)(p.proxy.dispatchCB(CreateProcessInstAction(Some(actionInfo.itemId), Some(actionInfo.participantId))))
                case _ =>
                  Callback.empty
              }
            }
          ),
          <.button("Cancel")
        )
      )
    }
  }

  private val component = ScalaComponent.builder[Props]("TicketFormComp")
    .initialStateFromProps(p => State())
    .renderBackend[Backend]
    .build

  def apply(proxy: ModelProxy[FormModel], c: RouterCtl[Loc], data: BaseActionInfoJson) = component(Props(proxy, c, data))
}
