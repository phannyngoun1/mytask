package com.dream.mytask.modules.workflow

import com.dream.mytask.AppClient.Loc
import com.dream.mytask.modules.workflow.WorkflowHandler.{FetchFlowAction, FetchFlowListAction, NewFlowAction}
import com.dream.mytask.services.DataModel.FlowModel
import com.dream.mytask.shared.data.WorkflowData.FlowJson
import diode.react.ReactPot._
import diode.react._
import japgolly.scalajs.react.{BackendScope, _}
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._

import scala.language.{implicitConversions, postfixOps}

object WorkflowComp {

  case class Props(proxy: ModelProxy[FlowModel], c: RouterCtl[Loc])

  case class State(id: Option[String] = None, name: Option[String] = None)

  class Backend($: BackendScope[Props, State]) {

    implicit def renderItem(item: FlowJson) = {
      <.li(s"id: ${item.id}, name: ${item.name}")
    }

    def render(p: Props, s: State) = {

      val wrapper = p.proxy.connect(_.message)
      val fetchWrapper = p.proxy.connect(_.flow)
      val list = p.proxy.connect(_.flowList)
      <.div("work flow",
        <.div("Flow list",
          <.div(
            list(px => {
              <.div(
                px().renderPending(_ > 500, _ => <.p("Loading...")),
                px().renderFailed(ex => <.p("Failed to load")),
                px().render(m => <.ol(^.`type` := "1",
                  m toTagMod
                ))
              )
            })
          )
        ),

        <.div("Creation",
          <.div(
            <.label("Name")
          ),
          <.div(<.input(^.`type` := "text", ^.value := s.name.getOrElse(""), ^.onChange ==> { e: ReactEventFromInput =>
            val value = if (e.target.value.trim.isEmpty) None else Some(e.target.value)
            $.modState(_.copy(name = value))
          })),
          <.div(
            <.button("New", ^.onClick --> Callback.when(s.name.isDefined)(p.proxy.dispatchCB(NewFlowAction(s.name))))
          ),
          <.div("New Flow: ",

            <.div(
              wrapper(px => {
                <.div(
                  px().renderPending(_ > 500, _ => <.p("Loading...")),
                  px().renderFailed(ex => <.p("Failed to load")),
                  px().render(m => <.p(s"hello ${m}"))
                )
              })
            )

          )
        ),

        <.div("Fetch Flow",
          <.div(
            <.label("Id"),
            <.input(^.`type` := "text", ^.value := s.id.getOrElse(""), ^.onChange ==> { e: ReactEventFromInput =>
              val value = if (e.target.value.trim.isEmpty) None else Some(e.target.value)
              $.modState(_.copy(id = value))
            })
          ),
          <.div(
            <.button("Fetch", ^.onClick --> Callback.when(s.id.isDefined)(p.proxy.dispatchCB(FetchFlowAction(s.id))))
          ),
          <.div("Result:"),
          <.div(
            fetchWrapper(px => {
              <.div(
                px().renderPending(_ > 500, _ => <.p("Loading...")),
                px().renderFailed(ex => <.p("Failed to load")),
                px().render(m => <.p(s"id: ${m.id}, name: ${m.name}"))
              )
            })
          )

        )

      )

    }
  }

  private val component = ScalaComponent.builder[Props]("WorkflowComp")
    .initialStateFromProps(p => State())
    .renderBackend[Backend]
    .componentDidMount(_.props.proxy.dispatchCB(FetchFlowListAction()))
    .build

  def apply(proxy: ModelProxy[FlowModel], c: RouterCtl[Loc]) = component(Props(proxy, c))

}
