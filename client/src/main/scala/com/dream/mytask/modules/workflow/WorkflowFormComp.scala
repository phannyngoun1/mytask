package com.dream.mytask.modules.workflow

import com.dream.mytask.AppClient.{Loc}
import com.dream.mytask.modules.workflow.WorkflowHandler.{ NewFlowAction}
import com.dream.mytask.services.DataModel.FlowModel
import com.dream.mytask.shared.data.WorkflowData.{FlowInitDataJs}
import diode.react._
import japgolly.scalajs.react.{BackendScope, _}
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._

object WorkflowFormComp {

  case class Props(proxy: ModelProxy[FlowModel], c: RouterCtl[Loc], data: FlowInitDataJs)

  case class State(
    id: Option[String] = None,
    name: Option[String] = None,
    participants: List[String] = List.empty,
    participantId: Option[String]= None
  )

  class Backend($: BackendScope[Props, State]) {

    def render(p: Props, s: State) = {
      <.div(
        <.h3("Creation"),
        <.div(
          <.label("Name")
        ),
        <.div(<.input(^.`type` := "text", ^.value := s.name.getOrElse(""), ^.onChange ==> { e: ReactEventFromInput =>
          val value = if (e.target.value.trim.isEmpty) None else Some(e.target.value)
          $.modState(_.copy(name = value))
        })),
        <.div(
          <.label("Participants"),
          <.ol(^.`type` := "1",
            s.participants toTagMod(item =>
              <.li(item)
              )
          ),
          <.div(
            <.select(
              ^.value := s.participantId.getOrElse(""),
              <.option(^.default := true),
              ^.onChange ==> { e: ReactEventFromInput =>
                val value = if (e.target.value.trim.isEmpty) None else Some(e.target.value)
                $.modState(_.copy(participantId = value))
              },
              p.data.pcpList.toTagMod{ item =>
                <.option(^.value := item.id, item.id)
              }
            ),
            <.button("Add", ^.onClick --> $.modState(m => m.copy(participants = s.participantId.get :: m.participants, participantId = None)))
          )
        ),
        <.div(
          <.button("New")
        )
      )
    }

  }

  private val component = ScalaComponent.builder[Props]("WorkflowFormComp")
    .initialStateFromProps(p => State())
    .renderBackend[Backend]
    .build

  def apply(proxy: ModelProxy[FlowModel], c: RouterCtl[Loc], data: FlowInitDataJs) = component(Props(proxy, c, data))

}
