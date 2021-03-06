package com.dream.mytask.modules.processinst

import java.util.UUID

import com.dream.mytask.AppClient.{DashboardLoc, Loc, PerformTaskLoc, ViewPInstLoc}
import com.dream.mytask.modules.form.FormActionHandler.{StartInstAction}
import com.dream.mytask.modules.processinst.ProcessInstActionHandler._
import com.dream.mytask.services.DataModel.ProcessInstanceModel
import diode.react.ReactPot._
import diode.react._
import japgolly.scalajs.react.{BackendScope, _}
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^.{<, _}


object ProcessInstComp {

  private val component = ScalaComponent.builder[Props]("DashboardModule")
    .initialStateFromProps(p => State())
    .renderBackend[Backend]
    .componentDidMount(_.props.proxy.dispatchCB(InitPInstAction()))
    .build

  def apply(proxy: ModelProxy[ProcessInstanceModel], c: RouterCtl[Loc]) = component(Props(proxy, c))

  case class Props(proxy: ModelProxy[ProcessInstanceModel], c: RouterCtl[Loc])

  case class State(pInstId: Option[String] = None, itemId: Option[String] = None, participantId: Option[String] = None)

  class Backend($: BackendScope[Props, State]) {


    def setAccId(e: ReactEventFromInput) = {
      val id = if (e.target.value.trim.isEmpty) None else Some(e.target.value)
      $.modState(_.copy(participantId = id))
    }

    def render(p: Props, s: State) = {

      val wrapper = p.proxy.connect(m => m)

      val listWrapper = p.proxy.connect(_.initData)

      <.div(
        <.div(
          <.div(^.textAlign := "Right",
            <.button("Back To Main", ^.onClick --> p.c.set(DashboardLoc))
          ),
          <.div(
            listWrapper(px => {
              <.div(
                <.div(
                  <.h4("Instance List"),
                  px().renderPending(_ > 500, _ => <.p("Loading...")),
                  px().renderFailed(ex => <.p("Failed to load")),
                  px().render(m =>
                    <.div(
                      <.ol(^.`type` := "1",
                        m.list toTagMod { item =>
                          <.li(<.a(p.c.setOnClick(ViewPInstLoc(UUID.fromString(item.id))), ^.href := "#", s"${item.id}"), s" : Folio: ${item.folio}")
                        }
                      ),
                      <.div(
                        <.h4("Create process instance"),
                        <.div(
                          <.label("Participant Id: "),
                          <.br(),
                          <.select(
                            ^.value := s.participantId.getOrElse(""),
                            ^.onChange ==> { e: ReactEventFromInput =>
                              val value = if (e.target.value.trim.isEmpty) None else Some(e.target.value)
                              $.modState(_.copy(participantId = value))
                            },
                            <.option(^.default := true),
                            m.pcpList.toTagMod { item =>
                              <.option(
                                ^.value := item.id,
                                item.id
                              )
                            },
                          )
                        ),
                        <.div(
                          <.label("item id: "),
                          <.br(),
                          <.select(
                            ^.onChange ==> { e: ReactEventFromInput =>
                              val value = if (e.target.value.trim.isEmpty) None else Some(e.target.value)
                              println(s"item selected: ${value}")
                              $.modState(_.copy(itemId = value))
                            },
                            ^.value := s.itemId.getOrElse(""),
                            <.option(^.default := true),
                            m.itemList.toTagMod { item =>
                              <.option(
                                ^.value := item.id.toString,
                                item.name
                              )
                            }
                          )
                        ),

                        <.div(
                          <.button("Create" , ^.onClick --> {
                            val participant = m.pcpList.find(_.id.equalsIgnoreCase(s.participantId.getOrElse("")))

                              val item = m.itemList
                                .find(_.id.equals(s.itemId.map(UUID.fromString).get))
                                .map(item => StartInstAction(
                                  item.initPayloadCode ,
                                  item.id,
                                  participant.map(p=> UUID.fromString(p.accountId)).get ,
                                  participant.map(p=> UUID.fromString(p.id)).get ))

                              (Callback(println(s"state: ${s.itemId}, ${s.participantId}")) >>
                                Callback.when(item.isDefined && s.participantId.isDefined)(p.proxy.dispatchCB(item.get) >> p.c.set(PerformTaskLoc)))
                          })
                        )
                      )
                    )
                  )
                )
              )
            })
          )
        )
      )
    }

    def setValue(e: ReactEventFromInput) = {
      val id = if (e.target.value.trim.isEmpty) None else Some(e.target.value)
      $.modState(_.copy(pInstId = id))
    }
  }
}
