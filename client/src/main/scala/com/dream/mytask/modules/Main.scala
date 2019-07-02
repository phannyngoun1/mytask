package com.dream.mytask.modules

import com.dream.mytask.AppClient.{AccLoc, FlowLoc, ItemLoc, Loc, ProcessInstLoc}
import com.dream.mytask.modules.common.Components._
import japgolly.scalajs.react.extra.router.{Resolution, RouterCtl}
import japgolly.scalajs.react.ScalaComponent
import japgolly.scalajs.react.component.Scala.BackendScope
import japgolly.scalajs.react.vdom.html_<^._

object Main {

  case class Props(c: RouterCtl[Loc], r: Resolution[Loc])
  case class State(st: String)

  class Backend($: BackendScope[Props, State]){

    def render(p: Props, s: State) = {
      <.div(
        <.nav(^.className := "navbar navbar-dark fixed-top bg-dark flex-md-nowrap p-0 shadow",
          <.a(^.className := "navbar-brand col-sm-3 col-md-2 mr-0", ^.href := "#",
            "My Tasks"
          ),
          <.input(^.className := "form-control form-control-dark w-100", ^.`type` := "text", ^.placeholder := "Search"),
          <.ul(^.className := "navbar-nav px-3",
            <.li(^.className := "nav-item text-nowrap", ^.className := "nav-link",
              <.a(^.href := "./api/secure/signout",
                "Sign Out"
              )
            )
          )
        ),
        <.div(^.className := "container-fluid",
          <.div(^.className := "row",
            <.nav(^.className := "col-md-2 d-none d-md-block bg-light sidebar",
              <.div(^.className := "sidebar-sticky",
                <.ul( ^.className := "nav flex-column",
                  <.li( ^.className := "nav-item",
                    <.a(^.className := "nav-link active", ^.href := "#",
                      <.span( dataFeather := "home" ),
                      "Dashboard",
                      <.span(^.className := "sr-only", "(current)")
                    )
                  ),
                  <.li( ^.className := "nav-item",
                    <.a(^.className := "nav-link ", ^.href := "#",
                      p.c.setOnClick(AccLoc),
                      <.span( dataFeather := "users" ),
                      "Account"
                    )
                  ),
                  <.li( ^.className := "nav-item",
                    <.a(^.className := "nav-link", ^.href := "#",
                      p.c.setOnClick(ItemLoc),
                      <.span( dataFeather := "list" ),
                      "Item"
                    )
                  ),
                  <.li( ^.className := "nav-item",
                    <.a(^.className := "nav-link", ^.href := "#",
                      p.c.setOnClick(FlowLoc),
                      <.span( dataFeather := "link-2" ),
                      "Flow"
                    )
                  ),
                  <.li( ^.className := "nav-item",
                    <.a(^.className := "nav-link", ^.href := "#",
                      <.span( dataFeather := "list" ),
                      p.c.setOnClick(ProcessInstLoc),
                      "Instance"
                    )
                  )
                )
              )
            ),
            <.main(^.role := "main", ^.className := "col-md-9 ml-sm-auto col-lg-10 px-4",
              <.div(^.className := "d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom",
                <.h3(^.className := "h4", "My Task"),
                <.div(^.className := "btn-toolbar mb-2 mb-md-0",
                  <.div(^.className := "btn-group mr-2",
                    <.button(^.`type` := "button", ^.className := "btn btn-sm btn-outline-secondary", "Share"),
                    <.button(^.`type` := "button", ^.className := "btn btn-sm btn-outline-secondary", "Export")
                  ),
                  <.button(^.`type` := "button", ^.className := "btn btn-sm btn-outline-secondary dropdown-toggle", "This week"),
                )
              ),
              <.div(p.r.render())
            )
          )
        )
      )

    }
  }

  private val component = ScalaComponent.builder[Props]("DashboardModule")
    .initialState(State("good"))
    .renderBackend[Backend]
    .build

  def apply(props: Props) = component(props)

}
