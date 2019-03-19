package com.dream.mytask.modules.account

import com.dream.mytask.AppClient.Loc
import com.dream.mytask.modules.account.AccountActionHandler.{FetchAccAction, FetchAccListAction}
import com.dream.mytask.modules.item.ItemActionHandler.NewItemAction
import com.dream.mytask.services.DataModel.AccountModel
import com.dream.mytask.shared.data.AccountData.AccountJson
import diode.react.ReactPot._
import diode.react._
import japgolly.scalajs.react.BackendScope
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react._

object AccountComp {

  case class Props(proxy: ModelProxy[AccountModel], c: RouterCtl[Loc])

  case class State(id: Option[String] = None, accName: Option[String] = None, desc: Option[String] = None)

  class Backend($: BackendScope[Props, State]) {


    implicit  def renderItem(item: AccountJson) = {
      <.li(s"id: ${item.id}, name: ${item.name}")
    }

    def render(p: Props, s: State) = {

      val fetchWrapper = p.proxy.connect(_.message)
      val listWrapper = p.proxy.connect(_.accountList)

      <.div("User",

        <.div("Account List",
          listWrapper(md => {
          <.div(
            md().renderPending(_ > 500, _ => <.p("Loading...")),
            md().renderFailed(ex => <.p("Failed to load")),
            md().render(m => <.ol( ^.`type` := "1",
              m toTagMod
            ))
          )})
        ),

        <.div("Message",

          fetchWrapper(md => {
            <.div(
              md().renderPending(_ > 500, _ => <.p("Loading...")),
              md().renderFailed(ex => <.p("Failed to load")),
              md().render(m => <.p(s"hello ${m}"))
            )
          })
        ),


        <.div("Get Item",
          <.div(
            <.input(^.`type` := "text", ^.value := s.id.getOrElse(""), ^.onChange ==> { e: ReactEventFromInput =>
              val value = if (e.target.value.trim.isEmpty) None else Some(e.target.value)
              $.modState(_.copy(id = value))
            })
          ),
          <.button("Fetch", ^.onClick --> Callback.when(s.id.isDefined)(
            p.proxy.dispatchCB(FetchAccAction(s.id)))
          )
        ),
        <.div("New",

          <.div(
            <.label("Name:"),
            <.input(^.`type` := "text", ^.value := s.accName.getOrElse(""), ^.onChange ==> { e: ReactEventFromInput =>
              val value = if (e.target.value.trim.isEmpty) None else Some(e.target.value)
              $.modState(_.copy(accName = value))
            })
          ),
          <.div(
            <.label("Description:"),
            <.input(^.`type` := "text", ^.value := s.desc.getOrElse(""), ^.onChange ==> { e: ReactEventFromInput =>
              val value = if (e.target.value.trim.isEmpty) None else Some(e.target.value)
              $.modState(_.copy(desc = value))
            })
          ),
          <.button(^.value := "New Account", ^.onClick --> Callback.when(s.id.isDefined)(
            p.proxy.dispatchCB(NewItemAction(s.accName, s.desc)))
          )
        )
      )
    }
  }

  private val component = ScalaComponent.builder[Props]("AccountComp")
    .initialStateFromProps(p => State())
    .renderBackend[Backend]
    .componentDidMount(_.props.proxy.dispatchCB(FetchAccListAction()))
    .build

  def apply(proxy: ModelProxy[AccountModel], c: RouterCtl[Loc]) = component(Props(proxy, c))
}
