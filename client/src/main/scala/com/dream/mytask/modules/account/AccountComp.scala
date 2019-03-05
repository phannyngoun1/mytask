package com.dream.mytask.modules.account

import com.dream.mytask.AppClient.Loc
import com.dream.mytask.shared.data.AccountData.AccountItem
import diode.data.Pot
import diode.react._
import japgolly.scalajs.react.BackendScope
import diode.react.ReactPot._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react._

object AccountComp {

  case class Props(proxy: ModelProxy[Pot[List[AccountItem]]], c: RouterCtl[Loc])

  case class State()

  class Backend($: BackendScope[Props, State]) {
    def render(p: Props, s: State) = {
      <.div()
    }
  }

  private val component = ScalaComponent.builder[Props]("AccountComp")
    .initialStateFromProps(p => State())
    .renderBackend[Backend]
    .build

  def apply(proxy: ModelProxy[Pot[List[AccountItem]]], c: RouterCtl[Loc]) = component(Props(proxy, c))
}
