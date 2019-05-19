package com.dream.mytask.modules.ticketform

import com.dream.mytask.AppClient.Loc
import com.dream.mytask.services.DataModel.FormModel
import com.dream.mytask.shared.data.ProcessInstanceData.PInstInitDataInfoJs
import com.dream.mytask.shared.data.TaskInfoJs
import diode.react._
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._


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
              <.td("Process Inst Id"), <.td(<.input(^.`type` := "text", ^.value := s"${m.pInstId}", ^.readOnly := true)),
              <.td("Folio"), <.td(<.input(^.`type` := "text", ^.value := m.folio, ^.readOnly := true))
            ),
            <.tr(
              <.td("Flow Id"), <.td(<.input(^.value := s"${m.flowId}", ^.readOnly := true)),
              <.td("Is Active"), <.td(<.input(^.value := s"${m.active}", ^.readOnly := true))
            )
          )
        ),

        <.ol(
          m.tasks.toTagMod(renderTask)
        )
      )
    }

    def renderTask(taskInfoJs: TaskInfoJs) = {
      <.li(s"Activity: ${taskInfoJs.activity}, Created Date: ${taskInfoJs.dateCreated} , Active: ${taskInfoJs.active} ",
        <.div(

          <.table(
            <.thead(
              <.tr(
                <.th("Participant"), <.th("Active")
              )
            ),
            <.tbody(
              taskInfoJs.destinations.toTagMod { dest =>
                <.tr(
                  <.td(dest.participantId.toString), <.td(s"${dest.active}")
                )
              }
            )
          ),

          <.table(^.border := "2px",
            <.thead(
              <.tr(
                <.th("Action"),
                <.th("Date Performed"),
                <.th("Participant"),
                <.th("Comment")
              ),
            ),
            <.tbody(
              taskInfoJs.actionPerformed.toTagMod { item =>
                <.tr(
                  <.td(item.action.name),
                  <.td(item.actionDate),
                  <.td(item.participantId.toString),
                  <.td(s"${item.comment.getOrElse("")}")
                )

              }
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
