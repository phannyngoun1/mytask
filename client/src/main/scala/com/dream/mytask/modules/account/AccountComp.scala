package com.dream.mytask.modules.account

import com.dream.mytask.AppClient.Loc
import com.dream.mytask.modules.account.AccountActionHandler._
import com.dream.mytask.services.DataModel.AccountModel
import com.dream.mytask.shared.data.AccountData.{AccountJson, ParticipantJson}
import diode.react.ReactPot._
import diode.react._
import japgolly.scalajs.react.BackendScope
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react._

import scala.language.implicitConversions
import scala.language.postfixOps

object AccountComp {

  case class Props(proxy: ModelProxy[AccountModel], c: RouterCtl[Loc])

  case class State(id: Option[String] = None, accountId: Option[String] = None , accName: Option[String] = None, fullName: Option[String] = None)

  class Backend($: BackendScope[Props, State]) {


    implicit  def renderItem(item: AccountJson) = {
      <.li(s"id: ${item.id}, name: ${item.name}")
    }

    implicit def renderParticipantItem(item: ParticipantJson) = {
      <.li(s"id: ${item.id}, account id: ${item.accountId}")
    }

    def render(p: Props, s: State) = {

      val messageWrapper = p.proxy.connect(_.message)
      val fetchAccountWrapper = p.proxy.connect(_.account)
      val fetchPPWrapper = p.proxy.connect(_.participant)
      val listWrapper = p.proxy.connect(_.accountList)
      val listParticipant = p.proxy.connect(_.participantList)

      <.div(
        <.h3("User"),
        <.h2("Account List"),
        <.div(
          listWrapper(md => {
          <.div(
            md().renderPending(_ > 500, _ => <.p("Loading...")),
            md().renderFailed(ex => <.p("Failed to load")),
            md().render(m => <.ol( ^.`type` := "1",
              m toTagMod
            ))
          )})
        ),

        <.h2("Participant List"),
        <.div(
          listParticipant(md => {
            <.div(
              md().renderPending(_ > 500, _ => <.p("Loading...")),
              md().renderFailed(ex => <.p("Failed to load")),
              md().render(m => <.ol( ^.`type` := "1",
                m toTagMod
              ))
            )})
        ),

        <.div(
          <.h3("Message"),
          messageWrapper(md => {
            <.div(
              md().renderPending(_ > 500, _ => <.p("Loading...")),
              md().renderFailed(ex => <.p("Failed to load")),
              md().render(m => <.p(s"message: ${m}"))
            )
          })
        ),

        <.div(
          <.h2("Get Data"),
          <.div(
            <.input(^.`type` := "text", ^.value := s.id.getOrElse(""), ^.onChange ==> { e: ReactEventFromInput =>
              val value = if (e.target.value.trim.isEmpty) None else Some(e.target.value)
              $.modState(_.copy(id = value))
            })
          ),
          <.button("Fetch Account", ^.onClick --> Callback.when(s.id.isDefined)(p.proxy.dispatchCB(FetchAccAction(s.id)))),
          <.button("Fetch Participant", ^.onClick --> Callback.when(s.id.isDefined){

            println("Fetch Participant click")

            p.proxy.dispatchCB(FetchParticipantAction(s.id))
          }

          ),
          <.div(
            fetchAccountWrapper(md => {
              <.div(
                md().renderPending(_ > 500, _ => <.p("Loading...")),
                md().renderFailed(ex => <.p("Failed to load")),
                md().render(m => <.p(s"accId: ${m.id}, acc name: ${m.name}, participant: ${m.currParticipantId}"))
              )
            })

          ),

          <.div(
            fetchPPWrapper(md => {
              <.div(
                md().renderPending(_ > 500, _ => <.p("Loading...")),
                md().renderFailed(ex => <.p("Failed to load")),
                md().render(m => <.p(s"participant id: ${m.id}, acc id: ${m.accountId}"))
              )
            })
          )
        ),
        <.div(
          <.h2("New account"),
          <.div(
            <.label("Name:"),
            <.input(^.`type` := "text", ^.value := s.accName.getOrElse(""), ^.onChange ==> { e: ReactEventFromInput =>
              val value = if (e.target.value.trim.isEmpty) None else Some(e.target.value)
              $.modState(_.copy(accName = value))
            })
          ),
          <.div(
            <.label("Full Name:"),
            <.input(^.`type` := "text", ^.value := s.fullName.getOrElse(""), ^.onChange ==> { e: ReactEventFromInput =>
              val value = if (e.target.value.trim.isEmpty) None else Some(e.target.value)
              $.modState(_.copy(fullName = value))
            })
          ),
          <.button("New Account", ^.onClick --> Callback.when(s.accName.isDefined && s.fullName.isDefined)(
            p.proxy.dispatchCB(NewAccAction(s.accName, s.fullName)))
          ),
          <.h2("New Participant"),
          <.div(
            <.label("Account Id:"),
            <.input(^.`type` := "text", ^.value := s.accountId.getOrElse(""), ^.onChange ==> { e: ReactEventFromInput =>
              val value = if (e.target.value.trim.isEmpty) None else Some(e.target.value)
              $.modState(_.copy(accountId = value))
            })
          ),

          <.button("New Participant", ^.onClick --> Callback.when(s.accountId.isDefined)(
            p.proxy.dispatchCB(NewParticipantAction(s.accountId)))
          )
        )
      )
    }
  }

  private val component = ScalaComponent.builder[Props]("AccountComp")
    .initialStateFromProps(p => State())
    .renderBackend[Backend]
    .componentDidMount(_.props.proxy.dispatchCB(FetchAccListAction()))
    .componentDidMount(_.props.proxy.dispatchCB(FetchParticipantListAction()))
    .build

  def apply(proxy: ModelProxy[AccountModel], c: RouterCtl[Loc]) = component(Props(proxy, c))
}
