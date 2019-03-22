package com.dream.mytask.modules.processinst

import com.dream.mytask.AppClient.Loc
import com.dream.mytask.modules.processinst.ProcessInstActionHandler._
import com.dream.mytask.services.DataModel.ProcessInstanceModel
import diode.react._
import japgolly.scalajs.react.BackendScope
import diode.react.ReactPot._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react._


object ProcessInstComp {

  case class Props(proxy: ModelProxy[ProcessInstanceModel], c: RouterCtl[Loc])
  case class State(pInstId: Option[String] = None, itemId: Option[String] = None, participantId: Option[String] = None)

  class Backend($: BackendScope[Props, State]) {


    def setValue(e: ReactEventFromInput) = {
      val id = if (e.target.value.trim.isEmpty) None else Some(e.target.value)
      $.modState(_.copy(pInstId =  id))
    }

    def setAccId(e: ReactEventFromInput) = {
      val id = if (e.target.value.trim.isEmpty) None else Some(e.target.value)
      $.modState(_.copy(participantId =  id))
    }



    def render(p: Props, s: State) = {
      val wrapper = p.proxy.connect(m => m)
      <.div(
        wrapper(proxy => {
          <.div(
            proxy().data.renderPending(_ > 500, _ => <.p("Loading...")),
            proxy().data.renderFailed(ex => <.p("Failed to load")),
            proxy().data.render(m => <.p( m.value))
          )
        }),

        <.div(
          <.div(
            <.label("Pinstance Id"),
            <.input(^.`type`  := "text", ^.value:= s.pInstId.getOrElse("") , ^.onChange ==>  setValue)
          ),
          <.button(^.onClick --> p.proxy.dispatchCB(FetchPInstAction(s.pInstId)), "Fetch")
        ),
        <.div(
          <.label("item id"),
          <.input(^.`type`  := "text", ^.value:= s.itemId.getOrElse("") , ^.onChange ==> { e: ReactEventFromInput =>
            val value = if (e.target.value.trim.isEmpty) None else Some(e.target.value)
            $.modState(_.copy(itemId = value))
          })
        ),

        <.div(
          <.label("Participant Id"),
          <.input(^.`type`  := "text", ^.value:= s.participantId.getOrElse("") ,  ^.onChange ==> { e: ReactEventFromInput =>
            val value = if (e.target.value.trim.isEmpty) None else Some(e.target.value)
            $.modState(_.copy(participantId = value))
          })
        ),
        <.div(
          <.button(^.onClick --> Callback.when(s.itemId.isDefined && s.participantId.isDefined)(p.proxy.dispatchCB(CreateProcessInstAction(s.itemId, s.participantId))), "Create")
        )
      )
    }
  }

  private val  component = ScalaComponent.builder[Props]("DashboardModule")
    .initialStateFromProps(p => State())
    .renderBackend[Backend]
    .build

  def apply(proxy: ModelProxy[ProcessInstanceModel], c: RouterCtl[Loc]) = component(Props(proxy, c))
}
