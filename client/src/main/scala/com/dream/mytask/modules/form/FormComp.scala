package com.dream.mytask.modules.form

import com.dream.mytask.AppClient.{FetchTaskLoc, Loc}
import com.dream.mytask.modules.ticketform.{AssignFormComp, CommentComp, TicketMainFormComp}
import com.dream.mytask.services.DataModel.FormModel
import com.dream.mytask.shared.data.ActionInfoJson
import diode.react._
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._

object FormComp {

  case class Props(proxy: ModelProxy[FormModel], c: RouterCtl[Loc])

  case class State()

  class Backend($: BackendScope[Props, State]) {

    def render(p: Props, s: State) = {

      val actionInfo = p.proxy.connect(_.actionInfo)

      actionInfo { item =>
        val data = item().get
        <.div(
          <.div(^.textAlign := "Right",
            <.button("Task List", ^.onClick --> p.c.set(FetchTaskLoc(data.accountId))
            )
          ),

          data.payloadCode match {
            case Some(v) =>
              if (v.contains("ticket-payload"))
                TicketMainFormComp(p.proxy, p.c, data)

              //Common forms
              else if (v.contains("assign-payload"))
                AssignFormComp(p.proxy, p.c, data.asInstanceOf[ActionInfoJson])
              else if (v.contains("comment-payload"))
                CommentComp(p.proxy, p.c, data.asInstanceOf[ActionInfoJson])
              else <.div("No form available.")
            case None => <.div("No Payload.")
          }

        )
      }
    }
  }

  private val component = ScalaComponent.builder[Props]("AssignFormComp")
    .initialStateFromProps(p => State())
    .renderBackend[Backend]
    .build

  def apply(proxy: ModelProxy[FormModel], c: RouterCtl[Loc]) = component(Props(proxy , c))

}
