package com.dream.mytask.modules.workflow

import java.util.UUID

import com.dream.mytask.AppClient.Loc
import com.dream.mytask.modules.workflow.WorkflowHandler.FetchWorkflowTemplateAction
import com.dream.mytask.services.DataModel.FlowModel
import diode.react.ReactPot._
import diode.react._
import japgolly.scalajs.react.{BackendScope, _}
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._

object WorkflowCreationComp {

  case class Props(proxy: ModelProxy[FlowModel], c: RouterCtl[Loc], id: Option[UUID])
  case class State()

  class Backend($: BackendScope[Props, State]) {

    def render(p: Props, s: State) = {

      val conn = p.proxy.connect(_.workflowTemplate)
      <.div(
        conn(px =>
          <.div(
            px().renderPending(_ > 500, _ => <.p("Loading...")),
            px().renderFailed(ex => <.p("Failed to load")),
            px().render(m =>
              <.div(WorkflowItemComp(p.proxy, p.c, m))
            )
          )
        )
      )
    }
  }

  private val component = ScalaComponent.builder[Props]("WorkflowCreationComp")
    .initialStateFromProps(p => State())
    .renderBackend[Backend]
    .componentDidMount(f => f.props.proxy.dispatchCB(FetchWorkflowTemplateAction(f.props.id)))
    .build

  def apply(proxy: ModelProxy[FlowModel], c: RouterCtl[Loc], id: Option[UUID]) = component(Props(proxy, c, id))

}
