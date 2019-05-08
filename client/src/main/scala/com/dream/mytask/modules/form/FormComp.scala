package com.dream.mytask.modules.form

import java.util.UUID

import com.dream.mytask.AppClient.{FetchTaskLoc, Loc}
import com.dream.mytask.services.DataModel.{AccountModel, FormModel}
import diode.react._
import diode.react.ReactPot._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react._

object FormComp {

  case class Props(proxy: ModelProxy[FormModel], c: RouterCtl[Loc])

  case class State()

  class Backend($: BackendScope[Props, State]) {

    def render(p: Props, s: State) = {

      val actionInfo = p.proxy.connect(_.actionInfo)

      actionInfo(item =>
        <.div(
          <.div(^.textAlign :="Right" ,
            <.button("Task List" , ^.onClick --> p.c.set(FetchTaskLoc(item().get.accountId))
            )
          ),
          "Taking action"
        )
      )
    }
  }

  private val component = ScalaComponent.builder[Props]("AssignFormComp")
    .initialStateFromProps(p => State())
    .renderBackend[Backend]
    .build

  def apply(proxy: ModelProxy[FormModel], c: RouterCtl[Loc]) = component(Props(proxy , c))

}
