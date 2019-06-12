package com.dream.mytask.modules.workflow

import java.util.UUID

import com.dream.mytask.AppClient.{DashboardLoc, Loc, ViewWorkflowLoc, WorkflowLoc}
import com.dream.mytask.modules.workflow.WorkflowHandler.InitFlowDataAction
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

  case class State(
    id: Option[String] = None,
    name: Option[String] = None,
    participants: List[String] = List.empty,
    participantId: Option[String]= None
  )

  class Backend($: BackendScope[Props, State]) {



    def render(p: Props, s: State) = {

//      val wrapper = p.proxy.connect(_.message)
//      val fetchWrapper = p.proxy.connect(_.flow)
      val list = p.proxy.connect(_.initData)
      <.div(
        <.div(^.textAlign :="Right" ,
          <.button("Back To Main", ^.onClick --> p.c.set(DashboardLoc))
        ),
        <.h2("work flow"),
        <.div(
          <.h3("Flow list"),
          <.div(
            list(px => {
              <.div(
                px().renderPending(_ > 500, _ => <.p("Loading...")),
                px().renderFailed(ex => <.p("Failed to load")),
                px().render(m =>
                  <.div(

                    <.ol(^.`type` := "1",
                      m.list toTagMod { item =>
                        <.li(s"name: ${item.name} ==> ",  <.a("View", ^.href := "#", p.c.setOnLinkClick(ViewWorkflowLoc(UUID.fromString(item.id)))) )
                      }
                    ),

                    <.div("Flow Template"),

                    <.ol(^.`type` := "1",
                      m.workflowTemplateList toTagMod(item =>
                        <.li(<.a(^.href := "#", p.c.setOnClick(WorkflowLoc(item.id)), item.name))
                        )
                    )
                  )
                )
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
    .componentDidMount(_.props.proxy.dispatchCB(InitFlowDataAction()))
    .build

  def apply(proxy: ModelProxy[FlowModel], c: RouterCtl[Loc]) = component(Props(proxy, c))

}
