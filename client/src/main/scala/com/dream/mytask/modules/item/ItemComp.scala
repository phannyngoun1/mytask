package com.dream.mytask.modules.item

import com.dream.mytask.AppClient.{DashboardLoc, Loc}
import com.dream.mytask.modules.item.ItemActionHandler._
import com.dream.mytask.services.DataModel.ItemModel
import com.dream.mytask.shared.data.ItemData.ItemJson
import diode.react._
import japgolly.scalajs.react.BackendScope
import diode.react.ReactPot._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react._

import scala.language.postfixOps
import scala.language.implicitConversions

object ItemComp {

  case class Props(proxy: ModelProxy[ItemModel], c: RouterCtl[Loc])

  case class State(id: Option[String] = None)

  class Backend($: BackendScope[Props, State]) {

    implicit  def renderItem(item: ItemJson) = {
      <.li(s"id: ${item.id}, name: ${item.name}")
    }


    def render(p: Props, s: State) = {
      val wrapper = p.proxy.connect(_.item)
      val message = p.proxy.connect(m=> m.message)
      val list = p.proxy.connect(_.initData)
      <.div(
        <.div(^.textAlign :="Right" ,
          <.button("Back To Main", ^.onClick --> p.c.set(DashboardLoc))
        ),
        <.h3("Item List:"),
        <.div(
          list(px => {
            <.div(
              px().renderPending(_ > 500, _ => <.p("Loading...")),
              px().renderFailed(ex => <.p("Failed to load")),
              px().render(m =>

                <.div(
                  <.ol( ^.`type` := "1",
                    m.list toTagMod
                  ),
                  ItemFormComp(p.proxy, p.c, m)
                )
              )
            )
          })
        ),
        <.div(
          <.label("item id"),
          <.input(^.`type` := "text", ^.value := s.id.getOrElse(""), ^.onChange ==> { e: ReactEventFromInput =>
            val id = if (e.target.value.trim.isEmpty) None else Some(e.target.value)
            $.modState(_.copy(id = id))
          })
        ),
        <.button("Fetch Item", ^.onClick --> p.proxy.dispatchCB(FetchItemAction(s.id))),
        <.div(
          <.h3("Result"),
          <.div(
            wrapper(px => {
              <.div(
                px().renderPending(_ > 500, _ => <.p("Loading...")),
                px().renderFailed(ex => <.p("Failed to load")),
                px().render(m => <.p( s"id: ${m.id}, name: ${m.name}"))
              )
            })
        )),
        <.div(
          <.div(
            <.h3("new item:"),
            <.div(
              message(px => {
                <.div(
                  px().renderPending(_ > 500, _ => <.p("Loading...")),
                  px().renderFailed(ex => <.p("Failed to load")),
                  px().render(m => <.p(s"hello ${m}"))
                )
              })
            )
          )
        )
      )
    }
  }

  private val component = ScalaComponent.builder[Props]("ItemComp")
    .initialStateFromProps(p => State())
    .renderBackend[Backend]
    .componentDidMount(_.props.proxy.dispatchCB(InitItemDataAction()))
    .build

  def apply(proxy: ModelProxy[ItemModel], c: RouterCtl[Loc]) = component(Props(proxy, c))

}
