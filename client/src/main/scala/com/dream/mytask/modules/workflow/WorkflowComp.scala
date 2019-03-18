package com.dream.mytask.modules.workflow

import com.dream.mytask.AppClient.Loc
import com.dream.mytask.modules.item.ItemActionHandler._
import com.dream.mytask.modules.item.ItemComp.{Props, State}
import com.dream.mytask.modules.workflow.WorkflowHandler.{FetchFlowAction, NewFlowAction}
import com.dream.mytask.services.DataModel.FlowModel
import com.dream.mytask.shared.data.ItemData.ItemJson
import diode.react._
import japgolly.scalajs.react.BackendScope
import diode.react.ReactPot._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react._

import scala.language.postfixOps
import scala.language.implicitConversions

object WorkflowComp {

  case class Props(proxy: ModelProxy[FlowModel], c: RouterCtl[Loc])

  case class State(id: Option[String]= None, name: Option[String] = None)

  class Backend($: BackendScope[Props, State]) {
    def render(p: Props, s: State) = {

      val wrapper = p.proxy.connect(_.message)
      val fetchWrapper = p.proxy.connect(_.flow)

      <.div("work flow",

        <.div( "Creation",
          <.div(
            <.label("Name")
          ),
          <.div(<.input(^.`type` := "text", ^.value := s.name.getOrElse(""), ^.onChange ==> { e: ReactEventFromInput =>
            val value = if (e.target.value.trim.isEmpty) None else Some(e.target.value)
            $.modState(_.copy(name = value))
          })),
          <.div(
            <.button("New", ^.onClick --> Callback.when(s.name.isDefined)(p.proxy.dispatchCB(NewFlowAction(s.name))) )
          ),
          <.div( "New Flow: ",

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
            <.input(^.`type` := "text", ^.value := s.id.getOrElse(""),^.onChange ==> { e: ReactEventFromInput =>
              val value = if (e.target.value.trim.isEmpty) None else Some(e.target.value)
              $.modState(_.copy(id = value))
            })
          ),
          <.div(
            <.button("Fetch", ^.onClick --> Callback.when(s.id.isDefined)(p.proxy.dispatchCB(FetchFlowAction(s.id))) )
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
    .componentDidMount(_.props.proxy.dispatchCB(FetchItemListAction()))
    .build

  def apply(proxy: ModelProxy[FlowModel], c: RouterCtl[Loc]) = component(Props(proxy, c))

}
