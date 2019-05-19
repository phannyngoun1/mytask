package com.dream.mytask.modules.ticketform

import com.dream.mytask.AppClient.{Loc}
import com.dream.mytask.modules.form.FormActionHandler.FetchPInstDataInfoAction
import com.dream.mytask.services.DataModel.FormModel
import com.dream.mytask.shared.data.ActionInfoJson
import diode.react._
import diode.react.ReactPot._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react._

object ViewFormComp {

  case class Props(proxy: ModelProxy[FormModel], c: RouterCtl[Loc], data: ActionInfoJson)

  case class State()

  class Backend($: BackendScope[Props, State]) {

    def render(p: Props, s: State) = {

      val pInstInfo = p.proxy.connect(_.ticketModel.pInstDataInfo)

      <.div(
        <.b("View Ticket data detail") ,
        <.div(
          pInstInfo(px => {
            <.div(
              px().renderPending(_ > 500, _ => <.p("Loading...")),
              px().renderFailed(_ => <.p("Failed to load")),
              px().render(m => <.div(ViewTicketDataDetailComp(p.proxy, p.c, m)))
            )
          })
        ),
        <.div(
          <.label("Comment:"),
          <.br(),
          <.textarea(^.cols := 60, ^.rows := 3)
        ),
        <.div(
          <.button("Submit"),
          <.button("Cancel")
        )
      )
    }
  }

  private val component = ScalaComponent.builder[Props]("TicketViewFormComp")
    .initialStateFromProps(p => State())
    .renderBackend[Backend]
    .componentDidMount( cmp => cmp.props.proxy.dispatchCB(FetchPInstDataInfoAction(
      pInstId = Option(cmp.props.data.pInstId),
      taskId = Option(cmp.props.data.taskId),
      accId = Option(cmp.props.data.accountId),
      participantId = Option(cmp.props.data.participantId),
    )))
    .build

  def apply(proxy: ModelProxy[FormModel], c: RouterCtl[Loc], data: ActionInfoJson) = component(Props(proxy, c, data))

}
