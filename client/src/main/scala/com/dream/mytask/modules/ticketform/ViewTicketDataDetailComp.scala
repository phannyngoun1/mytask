package com.dream.mytask.modules.ticketform

import com.dream.mytask.AppClient.{FetchTaskLoc, Loc}
import com.dream.mytask.services.DataModel.FormModel
import com.dream.mytask.shared.data.ActionInfoJson
import com.dream.mytask.shared.data.ProcessInstanceData.PInstInitDataInfoJs
import diode.react._
import diode.react.ReactPot._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react._


object ViewTicketDataDetailComp {

  case class Props(proxy: ModelProxy[FormModel], c: RouterCtl[Loc], data: PInstInitDataInfoJs)

  case class State()

  class Backend($: BackendScope[Props, State]) {

    def render(p: Props, s: State) = {

      val m = p.data
      <.div(
        <.table(
          <.tbody(
            <.tr(
              <.td("Process Inst Id"), <.td(<.input( ^.`type` := "text", ^.value := s"${m.pInstId}", ^.readOnly := true)),
              <.td("Folio"), <.td(<.input(^.`type` := "text", ^.value := m.folio , ^.readOnly := true))
            ),
            <.tr(
              <.td("Flow Id"), <.td(<.input(^.value := s"${m.flowId}" , ^.readOnly := true)),
              <.td("Is Active"), <.td(<.input(^.value := s"${m.active }", ^.readOnly := true))
            )
          )
        )
      )
    }

  }

  private val component = ScalaComponent.builder[Props]("TicketViewFormComp")
    .initialStateFromProps(p => State())
    .renderBackend[Backend]
    .build

  def apply(proxy: ModelProxy[FormModel], c: RouterCtl[Loc], data: PInstInitDataInfoJs) = component(Props(proxy, c, data))


}
