package com.dream.mytask.modules.workflow


import com.dream.mytask.AppClient.Loc
import com.dream.mytask.modules.workflow.WorkflowHandler.NewFlowAction
import com.dream.mytask.services.DataModel.FlowModel
import com.dream.mytask.shared.data.WorkflowData.{ActivityFlowJs, ContributionJs, WorkflowTemplateJs}
import diode.react._
import japgolly.scalajs.react.{BackendScope, _}
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._

import scala.util.Random

object WorkflowItemViewComp {
  case class Props(proxy: ModelProxy[FlowModel], c: RouterCtl[Loc], data: WorkflowTemplateJs)
  case class State( data: WorkflowTemplateJs, name: String , editingAct: Option[ActivityFlowJs] = None, contribute: Option[ContributionJs] = None)

  class Backend($: BackendScope[Props, State]) {

    val colors = List("#f951144f", "#3c00ff3b", "#16ea6942", "#b52cc742", "#1681ea42", "#b5161642", "#113a0d42", "#6cf35f42", "#b52cc742", "#f951144f", "#16ea6942")

    val rand = new Random(System.currentTimeMillis())

    private def edit(activityFlow: ActivityFlowJs)(e: ReactEventFromInput) = {
      e.preventDefault()
      $.modState(_.copy(editingAct = Some(activityFlow), contribute = None))
    }

    private def renderActivity(activityFlow: ActivityFlowJs, inx: Int) = {
      <.div(^.backgroundColor := s"${colors(inx)}",
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
        renderContribution(activityFlow)
      )
    }

    private def renderContribution(act: ActivityFlowJs) = {
      <.div(
        <.div( ^.className := "topnav", act.activityJs.name),
        <.div(
          <.table(^.className := "bottomBorder",
            <.thead(
              <.tr(
                <.th("Participant"),
                <.th("Policy"),
                <.th("Payload Auth Code"),
                <.th("Contribute. Type"),
                <.th("Actions"),
                <.th("")
              )
            ),
            <.tbody(
              act.contribution.toTagMod (item => {
                <.tr(
                  <.td(s"${item.participantId}"),
                  <.td(item.policyList.mkString("; ")),
                  <.td(item.payloadAuthCode),
                  <.td(item.contributeTypeList.mkString("; ")),
                  <.td(item.accessibleActionList.map(action => s"${action.name} |  ${action.actionType}" ).mkString("; "))
                )
              })
            )
          )
        )
      )
    }

    def render(p: Props, s: State) = {
      <.div(
        <.table(
          <.tbody(
            <.tr(
              <.td(^.textAlign := "top"  ,<.h3("Flow Name")),
              <.td(
                <.input(^.`type` := "text", ^.value := s.name, ^.onChange ==> { e: ReactEventFromInput =>
                  val value = e.target.value.trim
                  $.modState(_.copy(name = value))
                })
              )
            ),
            <.tr(
              <.td(^.textAlign := "top"  ,  <.h3("Flows")),
              <.td(
                s.data.activityFlowList.zipWithIndex.toTagMod ( f=> renderActivity(f._1, f._2))
              )
            )
          )
        )
      )
    }
  }

  private val component = ScalaComponent.builder[Props]("WorkflowItemViewComp")
    .initialStateFromProps(p => State(data = p.data, name = p.data.name))
    .renderBackend[Backend]
    .build

  def apply(proxy: ModelProxy[FlowModel], c: RouterCtl[Loc], data: WorkflowTemplateJs) =
    component(Props(proxy, c,  data))
}
