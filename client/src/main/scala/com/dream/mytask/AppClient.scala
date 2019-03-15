package com.dream.mytask

import com.dream.mytask.modules.account.AccountComp
import com.dream.mytask.modules.item.ItemComp
import com.dream.mytask.modules.processinst.ProcessInstComp
import com.dream.mytask.modules.task.TaskListComp
import com.dream.mytask.modules.workflow.WorkflowComp
import com.dream.mytask.modules.{Dashboard, Main}
import com.dream.mytask.services.AppCircuit
import japgolly.scalajs.react.extra.router._
import org.scalajs.dom

object AppClient {

  val routerConfig = RouterConfigDsl[Loc].buildConfig { dsl =>
    import dsl._

    val taskListRoute = staticRoute("#tasks", TaskListLoc) ~> renderR( c=> AppCircuit.wrap(_.taskModel.taskList)(proxy => TaskListComp(proxy, c)))
    val processInstRoute = staticRoute("#pinst", ProcessInstLoc) ~> renderR( c=> AppCircuit.wrap(_.processInst)(proxy => ProcessInstComp(proxy, c)))
    val itemRoute = staticRoute("#item", ItemLoc) ~> renderR( c=> AppCircuit.wrap(_.itemModel)(proxy => ItemComp(proxy, c)))
    val accComp = staticRoute("#acc", AccLoc) ~> renderR( c=> AppCircuit.wrap(_.accountModel)(proxy => AccountComp(proxy, c)))
    val flowComp = staticRoute("#flow", FlowLoc) ~> renderR( c=> AppCircuit.wrap(_.flowModel)(proxy => WorkflowComp(proxy, c)))

    (
      staticRoute(root, DashboardLoc) ~> renderR(c => AppCircuit.wrap(_.message)(proxy => Dashboard(proxy, c)))
      | taskListRoute
      | processInstRoute
      | itemRoute
      | accComp
      | flowComp
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

  case object TaskListLoc extends Loc

  case object ProcessInstLoc extends Loc

  case object ItemLoc extends Loc

  case object AccLoc  extends Loc

  case object FlowLoc  extends Loc
}
