package com.dream.mytask.modules.workflow

import com.dream.mytask.AppClient.Loc
import com.dream.mytask.modules.item.ItemActionHandler._
import com.dream.mytask.modules.item.ItemComp.{Props, State}
import com.dream.mytask.services.DataModel.FlowModel
import com.dream.mytask.shared.data.ItemData.ItemJson
import diode.react._
import japgolly.scalajs.react.BackendScope
import diode.react.ReactPot._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react._

import scala.language.postfixOps
import scala.language.implicitConversions

object WorkflowComp {

  case class Props(proxy: ModelProxy[FlowModel], c: RouterCtl[Loc])

  case class State()

  class Backend($: BackendScope[Props, State]) {
    def render(p: Props, s: State) = {
      <.div("work flow")
    }
  }

  private val component = ScalaComponent.builder[Props]("WorkflowComp")
    .initialStateFromProps(p => State())
    .renderBackend[Backend]
    .componentDidMount(_.props.proxy.dispatchCB(FetchItemListAction()))
    .build

  def apply(proxy: ModelProxy[FlowModel], c: RouterCtl[Loc]) = component(Props(proxy, c))

}
