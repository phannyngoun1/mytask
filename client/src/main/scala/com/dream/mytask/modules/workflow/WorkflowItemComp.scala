package com.dream.mytask.modules.workflow

import com.dream.mytask.AppClient.Loc
import com.dream.mytask.modules.workflow.WorkflowHandler.{FetchWorkflowTemplateAction, NewFlowAction}
import com.dream.mytask.services.DataModel.FlowModel
import com.dream.mytask.shared.data.WorkflowData.{ActivityFlowJs, ContributionJs, FlowInitDataJs, WorkflowTemplateJs}
import diode.data.Pot
import diode.react.ReactPot._
import diode.react._
import japgolly.scalajs.react.{BackendScope, _}
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._

import scala.util.Random

object WorkflowItemComp {
  case class Props(proxy: ModelProxy[FlowModel], c: RouterCtl[Loc], data: WorkflowTemplateJs)
  case class State(map: Map[ActivityFlowJs, List[ContributionJs]] = Map.empty)

  class Backend($: BackendScope[Props, State]) {

    val colors = List("#f951144f", "#3c00ff3b", "#16ea6942", "#b52cc742", "#1681ea42", "#b5161642", "#113a0d42", "#6cf35f42")

    val rand = new Random(System.currentTimeMillis())


    private def renderActivity(activityFlow: ActivityFlowJs) = {
      val random_index = rand.nextInt(colors.length)
      val result = colors(random_index)
      <.div(^.backgroundColor := s"${result}",
        <.h3(s"Activity Name: ${activityFlow.activityJs.name}"),
        <.h3(s"Actions List:"),
        <.ol(
          activityFlow.actionFlow.toTagMod(item =>
            <.div(
              <.li(s"Action: ${item.action.name}, Action Type: ${item.activity.map(_.name).getOrElse("N/A")}, =====> Next activity: ${item.activity.map(_.name).getOrElse("None")}")
            )
          )
        ),
        <.h3(s"Contributions:"),


      )
    }

    def render(p: Props, s: State) = {
      <.div(

        <.table(
          <.tbody(
            <.tr(
              <.td("Id"), <.td(p.data.id.toString)
            ),
            <.tr(
              <.td("Template Name"), <.td(p.data.name)
            ),
            <.tr(
              <.td("Flows"),
              <.td(
                p.data.activityFlowList.toTagMod (renderActivity)
              )
            )
          )
        )

      )
    }
  }

  private val component = ScalaComponent.builder[Props]("WorkflowItemComp")
    .initialStateFromProps(p => State())
    .renderBackend[Backend]
    .build

  def apply(proxy: ModelProxy[FlowModel], c: RouterCtl[Loc], data: WorkflowTemplateJs) = component(Props(proxy, c, data))
}
