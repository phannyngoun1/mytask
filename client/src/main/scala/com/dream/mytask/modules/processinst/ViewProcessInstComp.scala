package com.dream.mytask.modules.processinst

import java.util.UUID

import com.dream.mytask.AppClient.{Loc, ProcessInstLoc}
import com.dream.mytask.modules.form.FormActionHandler.FetchPInstDataInfoAction
import com.dream.mytask.modules.ticketform.ViewTicketDataDetailComp
import com.dream.mytask.services.DataModel.FormModel
import diode.react._
import diode.react.ReactPot._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react._

object ViewProcessInstComp {

  case class Props(proxy: ModelProxy[FormModel], c: RouterCtl[Loc], pInstId: Option[UUID])

  case class State()

  class Backend($: BackendScope[Props, State]) {

    def render(p: Props, s: State) = {

      val pInstInfo = p.proxy.connect(_.ticketModel.pInstDataInfo)

      <.div(
        <.div(^.textAlign := "Right",
          <.button("Back", ^.onClick --> p.c.set(ProcessInstLoc))
        ),
        "View Ticker data form",
        <.div(
          pInstInfo(px => {
            <.div(
              px().renderPending(_ > 500, _ => <.p("Loading...")),
              px().renderFailed(_ => <.p("Failed to load")),
              px().render(m => <.div( ViewTicketDataDetailComp(p.proxy, p.c, m) ))
            )
          })
        )
      )
    }
  }

  private val component = ScalaComponent.builder[Props]("ViewProcessInstComp")
    .initialStateFromProps(p => State())
    .renderBackend[Backend]
    .componentDidMount( cmp =>  Callback.when( cmp.props.pInstId.isDefined) (cmp.props.proxy.dispatchCB(FetchPInstDataInfoAction(
      pInstId = cmp.props.pInstId,
      taskId = Some(UUID.randomUUID()),
      accId = Some(UUID.randomUUID()),
      participantId = Some(UUID.randomUUID())
    ))))
    .build

  def apply(proxy: ModelProxy[FormModel], c: RouterCtl[Loc], pInstId:  Option[UUID]) = component(Props(proxy, c, pInstId))

}
