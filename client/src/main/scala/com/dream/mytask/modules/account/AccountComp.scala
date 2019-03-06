package com.dream.mytask.modules.account

import com.dream.mytask.AppClient.Loc
import com.dream.mytask.services.DataModel.AccountModel
import diode.data.Pot
import diode.react._
import japgolly.scalajs.react.BackendScope
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react._

object AccountComp {

  case class Props(proxy: ModelProxy[AccountModel], c: RouterCtl[Loc])

  case class State()

  class Backend($: BackendScope[Props, State]) {
    def render(p: Props, s: State) = {
      <.div("account")
    }
  }

  private val component = ScalaComponent.builder[Props]("AccountComp")
    .initialStateFromProps(p => State())
    .renderBackend[Backend]
    .build

  def apply(proxy: ModelProxy[AccountModel], c: RouterCtl[Loc]) = component(Props(proxy, c))
}
