package com.dream.mytask

import com.dream.mytask.modules.{Dashboard, Main}
import com.dream.mytask.services.AppCircuit
import japgolly.scalajs.react.extra.router._
import org.scalajs.dom

object AppClient {

  val routerConfig = RouterConfigDsl[Loc].buildConfig { dsl =>
    import dsl._
    (
      staticRoute(root, DashboardLoc) ~> renderR(c => AppCircuit.wrap(_.message)(proxy => Dashboard(proxy, c)))
      ).notFound(redirectToPage(DashboardLoc)(Redirect.Replace))

  }.renderWith(layout)
  val searchingWrapper = AppCircuit.connect(_.message)

  def layout(c: RouterCtl[Loc], r: Resolution[Loc]) = {
    searchingWrapper(proxy => Main(Main.Props(c, r)))
  }

  def main(args: Array[String]): Unit = {

    //    val NoArgs = ScalaComponent.builder[Unit]("No args")
    //      .renderStatic(<.div("Hello!"))
    //      .build
    //    NoArgs().renderIntoDOM (dom.document.getElementById("root"))

    val router = Router(BaseUrl.until_#, routerConfig)
    router().renderIntoDOM(dom.document.getElementById("root"))

  }

  sealed trait Loc

  case object DashboardLoc extends Loc
}
