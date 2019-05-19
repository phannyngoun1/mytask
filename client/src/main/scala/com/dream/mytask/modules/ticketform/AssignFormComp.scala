package com.dream.mytask.modules.ticketform

import java.util.UUID

import com.dream.mytask.AppClient.{FetchTaskLoc, Loc}
import com.dream.mytask.modules.form.FormActionHandler.FormAction
import com.dream.mytask.modules.ticketform.TicketActionHandler.InitAssignFormAction
import com.dream.mytask.services.DataModel.FormModel
import com.dream.mytask.shared.data.ActionInfoJson
import com.dream.mytask.shared.data.WorkflowData.{AssignTicketPayloadJs, CommentPayloadJs}
import diode.react._
import diode.react.ReactPot._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react._

object AssignFormComp {

  case class Props(proxy: ModelProxy[FormModel], c: RouterCtl[Loc], data: ActionInfoJson)

  case class State( participantId: Option[UUID] = None, comment: Option[String] = None )

  class Backend($: BackendScope[Props, State]) {
    def render(p: Props, s: State) = {

      val message = p.proxy.connect(_.message)
      val initData = p.proxy.connect(_.ticketModel.assignFormInitData)

      <.div(
        "Assign form",

        message(px => <.div(
          px().renderPending(_ > 500, _ => <.p("Loading...")),
          px().renderFailed(ex => <.p(s"Failed to load, ${ex}")),
          px().render(m => <.div(^.color := "blue" , m) )
        )),

        initData(px => <.div(
          px().renderPending(_ > 500, _ => <.p("Loading...")),
          px().renderFailed(ex => <.p(s"Failed to load, ${ex}")),
          px().render { m =>
            <.div(
              <.div(
                <.label("User:"),
                <.br(),
                <.select(
                  ^.value := s.participantId.map(_.toString).getOrElse(""),
                  <.option(^.default := true),
                  ^.onChange ==> { e: ReactEventFromInput =>
                    val value = if (e.target.value.trim.isEmpty) None else Some(e.target.value)
                    $.modState(_.copy(participantId = value.map(UUID.fromString _)))
                  },
                  m.participantList.toTagMod{ item =>
                    <.option(^.value := item.id, item.id)
                  }
                )
              ),
              <.div(
                <.label("Comment:"),
                <.br(),
                <.textarea(
                  ^.cols := 60,
                  ^.rows := 3,
                  ^.value := s.comment.getOrElse(""),
                  ^.onChange ==> { e: ReactEventFromInput =>
                    val value = if (e.target.value.trim.isEmpty) None else Some(e.target.value)
                    $.modState(_.copy(comment = value))
                  }
                )
              ),
            )

          }
        )),
        <.div(
          <.button("Submit",
            ^.onClick --> Callback.when(s.participantId.isDefined) (p.proxy.dispatchCB(FormAction(
              pInstId       = Some(p.data.pInstId.toString),
              taskId        = Some(p.data.taskId.toString),
              accId         = Some(p.data.accountId.toString),
              participantId = Some(p.data.participantId.toString),
              action        = Some(p.data.action),
              payLoad       = Some(AssignTicketPayloadJs(s.participantId.get,s.comment))
            )))
          ),
          <.button("Cancel")
        )
      )
    }
  }

  private val component = ScalaComponent.builder[Props]("AssignFormComp")
    .initialStateFromProps(p => State())
    .renderBackend[Backend]
    .componentDidMount(f => f.props.proxy.dispatchCB(InitAssignFormAction()))
    .build

  def apply(proxy: ModelProxy[FormModel], c: RouterCtl[Loc], data: ActionInfoJson) = component(Props(proxy, c, data))

}
