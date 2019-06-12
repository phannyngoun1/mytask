package com.dream.mytask.modules.workflow


import java.util.UUID

import com.dream.mytask.AppClient.Loc
import com.dream.mytask.modules.workflow.WorkflowHandler.NewFlowAction
import com.dream.mytask.services.DataModel.FlowModel
import com.dream.mytask.shared.data.WorkflowData.{ActivityFlowJs, ContributionJs, WorkflowTemplateJs}
import diode.react._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{BackendScope, _}

import scala.util.Random

object WorkflowItemComp {

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
        <.h3(s"Activity Name: ${activityFlow.activityJs.name} ###", <.a(^.href := "#", "Edit", ^.onClick ==>   edit(activityFlow))),
        <.h3(s"Actions List:"),
        <.ol(
          activityFlow.actionFlow.toTagMod(item =>
            <.div(
              <.li(s"Action: ${item.action.name}, Action Type: ${item.activity.map(_.name).getOrElse("N/A")}, =====> Next activity: ${item.activity.map(_.name).getOrElse("None")}")
            )
          )
        ),
        <.h3(s"Contributions:")
      )
    }

    private def editForm(act: ActivityFlowJs, state: State) = {
      <.div(
        <.div( ^.className := "topnav", act.activityJs.name , "-----", <.a(
          ^.href := "#", "==> Commit",
          ^.onClick ==> { e: ReactEventFromInput =>
            e.preventDefault()
            Callback.when(state.editingAct.isDefined)(
            $.modState(m => m.copy(data = m.data.copy(activityFlowList = m.data.activityFlowList.map(m => {
              if(m.activityJs == act.activityJs)
                state.editingAct.get
              else m.copy()
            }))))
            )
          }
        )),
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
                  <.td(item.accessibleActionList.map(action => s"${action.name} |  ${action.actionType}" ).mkString("; ")),
                  <.td(<.a(
                    ^.href := "#", "Remove",
                    ^.onClick ==> { e: ReactEventFromInput =>
                      e.preventDefault()
                      $.modState(m => m.copy(editingAct = m.editingAct.map(act => act.copy(contribution = act.contribution.filter(f => f != item) ))))
                    }
                  ))
                )
              })
            )
          )
        )
      )
    }

    private def removeActionList(e: ReactEventFromInput) = {
      e.preventDefault()
      $.modState(m => m.copy(contribute = m.contribute.map(item => item.copy(accessibleActionList = List.empty)) ))
    }

    private  def entryForm( act: ActivityFlowJs, p: Props, state: State) = {

      val ppp = UUID.randomUUID().toString

      <.tr(
        <.td(^.textAlign := "top"  ,<.h3("Contribute Form")),
        <.td(
          <.table(
            <.tbody(
              <.tr(
                <.td("Participant: "),
                <.td(
                  <.select(
                    ^.value := state.contribute.map(_.participantId.toString).getOrElse("") ,
                    ^.onChange ==> { e: ReactEventFromInput =>
                      val value = if (e.target.value.trim.isEmpty) None else Some(e.target.value)
                      value.map(v =>  $.modState(_.copy(contribute = Some(ContributionJs(UUID.fromString(v))))))
                        .getOrElse($.modState(_.copy(contribute = None)))
                    },
                    <.option(^.default := true),
                    p.data.flowInitDataJs
                      .map(_.pcpList.filter(pcp => !state.editingAct.map ( _.contribution.exists( _.participantId.toString.equals(pcp.id) )).getOrElse(false) )
                        .toTagMod(item =>  <.option(^.value := item.id ,s"${item.id}-${item.accountId}"))
                      ).getOrElse(VdomArray.empty())

                  )
                ),
                <.td(s"${state.contribute.map(_.participantId.toString).getOrElse("")}")
              ),
              <.tr(
                <.td("Policy: "),
                <.td(
                  <.select(
                    <.option("Hello")
                  ),
                  <.button(">>")
                ),
                <.td( "Policy list ----", <.a(^.href := "#", "Remove"))
              ),
              <.tr(
                <.td("Payload Auth Code: "),
                <.td(<.input()),
                <.td()
              ),
              <.tr(
                <.td("Contribute. Type: "),
                <.td(
                  <.select(
                    ^.onChange ==> { e: ReactEventFromInput =>

                      val value = if(e.target.value.trim.isEmpty) None else Some(e.target.value.trim)
                      value
                        .map(v =>$.modState(m => m.copy(contribute = m.contribute.map(item => item.copy(contributeTypeList =   v :: item.contributeTypeList)))))
                        .getOrElse($.modState(_.copy()))
                    },
                    ^.value :="",
                    <.option(^.default := true, "Add", ^.width := "200"),
                    act.contributeTypes.filter(item => !state.contribute.map ( _.contributeTypeList.exists( _.equals(item.code) )).getOrElse(false))
                      .toTagMod(t => <.option(^.value := t.code, ^.width := "200" , t.name))
                  )
                ),
                <.td(
                  s"${state.contribute.map(_.contributeTypeList.mkString("; " ) ).getOrElse("N/A")}",
                  <.a(
                    ^.href := "#", "Remove",
                    ^.onClick ==> { e: ReactEventFromInput =>
                      e.preventDefault()
                      $.modState(m => m.copy(contribute = m.contribute.map(item => item.copy(contributeTypeList = List.empty)) ))
                    }
                  ))
              ),
              <.tr(
                <.td("Action: "),
                <.td(
                  <.select(
                    ^.value :="",
                    <.option(^.default := true, "Add Action"),
                    ^.onChange ==> { e: ReactEventFromInput =>
                        act.actionFlow
                          .find(_.action.name.equalsIgnoreCase(e.target.value.trim))
                          .map(actionFlow => $.modState( m =>
                            m.copy(contribute = m.contribute.map(item => item.copy(accessibleActionList =   actionFlow.action :: item.accessibleActionList))))
                          )
                          .getOrElse($.modState( _.copy()))
                    },

                    act.actionFlow.filter(item => !state.contribute.map ( _.accessibleActionList.exists( _.equals(item.action) )).getOrElse(false))
                        .toTagMod(act => <.option(^.value := act.action.name , act.action.name))
                  )
                ),
                <.td(
                  s"${state.contribute.map(_.accessibleActionList.mkString("; " ) ).getOrElse("N/A")}",
                  <.a(
                    ^.onClick ==>removeActionList,
                    ^.href := "#", "Remove"
                  )
                )
              ),
              <.tr(
                <.td( ^.colSpan := 3, ^.textAlign := "left",
                  <.button("Add",
                    ^.onClick ==> { e: ReactEventFromInput =>
                      e.preventDefault()
                      Callback.when(state.contribute.isDefined)($.modState(m => m.copy(
                        editingAct = m.editingAct.map(act=> act.copy(
                          contribution = (m.contribute.get :: act.contribution)
                        )),
                        contribute = None
                      )))
                    }
                  )
                )
              )
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
              <.td(^.textAlign := "top"  ,<.h3("Id")), <.td(p.data.id.toString)
            ),
            <.tr(
              <.td(^.textAlign := "top"  ,<.h3("Template Name")), <.td(p.data.name)
            ),
            <.tr(
              <.td(^.textAlign := "top"  ,<.h3("New Flow Name")),
              <.td(
                <.input(^.`type` := "text", ^.value := s.name, ^.onChange ==> { e: ReactEventFromInput =>
                  val value = e.target.value.trim
                  $.modState(_.copy(name = value))
                }),

                <.a("==> Commit", ^.href := "#", ^.onClick ==> {  e: ReactEventFromInput =>
                  e.preventDefault()

                  p.proxy.dispatchCB(NewFlowAction(Some(s.data.copy(name = s.name))))

                  Callback(println(" ------- Hello ------- "))
                })
              )
            ),
            <.tr(
              <.td(^.textAlign := "top"  ,  <.h3("Flows")),
              <.td(
                s.data.activityFlowList.zipWithIndex.toTagMod ( f=> renderActivity(f._1, f._2))
              )
            ),
            <.tr(
              <.td(^.textAlign := "top"  ,<.h3("Edit Activity")),
              <.td(
                s.editingAct.map(editForm(_, s)).getOrElse(<.div("No activity selected"))
              )
            ),

            s.editingAct.map(entryForm(_, p, s) ).getOrElse(VdomArray.empty())
          )
        )
      )
    }
  }

  private val component = ScalaComponent.builder[Props]("WorkflowItemComp")
    .initialStateFromProps(p => State(data = p.data, name = p.data.name))
    .renderBackend[Backend]
    .build

  def apply(proxy: ModelProxy[FlowModel], c: RouterCtl[Loc], data: WorkflowTemplateJs)
  = component(Props(proxy, c,  data))
}
