package com.dream.mytask.modules.item

import com.dream.mytask.AppClient.{DashboardLoc, Loc}
import com.dream.mytask.modules.item.ItemActionHandler._
import com.dream.mytask.services.DataModel.ItemModel
import com.dream.mytask.shared.data.ItemData.{ItemInitDataJs, ItemJson}
import diode.react._
import japgolly.scalajs.react.BackendScope
import diode.react.ReactPot._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react._


object ItemFormComp {

  case class Props(proxy: ModelProxy[ItemModel], c: RouterCtl[Loc], data: ItemInitDataJs)

  case class State(id: Option[String] = None, flowId: Option[String] = None, itemName: Option[String] = None, desc: Option[String] = None )

  class Backend($: BackendScope[Props, State]) {

    def render(p: Props, s: State) = {
      <.div(
        <.div(
          <.div(
            <.label("name:"),
            <.br(),
            <.input(^.`type` := "text", ^.value := s.itemName.getOrElse(""), ^.onChange ==> { e: ReactEventFromInput =>
              val value = if (e.target.value.trim.isEmpty) None else Some(e.target.value)
              $.modState(_.copy(itemName = value))
            })),
          <.div(
            <.label("Flow ID:"),
            <.br(),
            <.select(
              ^.value := s.flowId.getOrElse(""),
              ^.onChange ==> { e: ReactEventFromInput =>
                val value = if (e.target.value.trim.isEmpty) None else Some(e.target.value)
                $.modState(_.copy(flowId = value))
              },
              <.option(^.default := true),
              p.data.flowList.toTagMod { item =>
                <.option(^.value := item.id, item.name)
              }
            )),
          <.div(
            <.label("description:"),
            <.br(),
            <.input(^.`type` := "text", ^.value := s.desc.getOrElse(""), ^.onChange ==> { e: ReactEventFromInput =>
              val value = if (e.target.value.trim.isEmpty) None else Some(e.target.value)
              $.modState(_.copy(desc = value))
            })
          )
        ),

        <.button("Create Item", ^.onClick --> Callback.when(s.itemName.isDefined && s.desc.isDefined)(
          p.proxy.dispatchCB(NewItemAction(s.itemName, s.flowId, s.desc)) >>  p.proxy.dispatchCB(FetchItemListAction())
        ))
      )
    }

  }

  private val component = ScalaComponent.builder[Props]("ItemFormComp")
    .initialStateFromProps(p => State())
    .renderBackend[Backend]
    .build

  def apply(proxy: ModelProxy[ItemModel], c: RouterCtl[Loc], data: ItemInitDataJs) = component(Props(proxy, c, data))


}
