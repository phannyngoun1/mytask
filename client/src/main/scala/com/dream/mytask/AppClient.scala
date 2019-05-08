package com.dream.mytask

import java.util.UUID

import com.dream.mytask.modules.account.AccountComp
import com.dream.mytask.modules.form.FormComp
import com.dream.mytask.modules.item.ItemComp
import com.dream.mytask.modules.processinst.ProcessInstComp
import com.dream.mytask.modules.task.TaskListComp
import com.dream.mytask.modules.ticketform.AssignFormComp
import com.dream.mytask.modules.workflow.WorkflowComp
import com.dream.mytask.modules.{Dashboard, Main}
import com.dream.mytask.services.AppCircuit
import japgolly.scalajs.react.extra.router._
import org.scalajs.dom

object AppClient {

  val routerConfig = RouterConfigDsl[Loc].buildConfig { dsl =>
    import dsl._

    val taskListRoute = staticRoute("#tasks", TaskListLoc) ~> renderR( c => AppCircuit.wrap(_.taskModel)(proxy => TaskListComp(proxy, c)))
    val processInstRoute = staticRoute("#instance", ProcessInstLoc) ~> renderR( c => AppCircuit.wrap(_.processInst)(proxy => ProcessInstComp(proxy, c)))
    val itemRoute = staticRoute("#item", ItemLoc) ~> renderR( c => AppCircuit.wrap(_.itemModel)(proxy => ItemComp(proxy, c)))
    val accRoute = staticRoute("#account", AccLoc) ~> renderR( c => AppCircuit.wrap(_.accountModel)(proxy => AccountComp(proxy, c)))
    val flowRoute = staticRoute("#flow", FlowLoc) ~> renderR( c => AppCircuit.wrap(_.flowModel)(proxy => WorkflowComp(proxy, c)))
    val fetchTaskRoute = dynamicRouteCT( ("#fetchTask" / uuid ).caseClass[FetchTaskLoc]  ) ~> dynRenderR( (p, c)=>{
      val id = p match {
        case FetchTaskLoc(id) => Some(id)
        case _ => None
      }
      AppCircuit.wrap(_.taskModel) (proxy => TaskListComp(proxy, c, id))
    })

    val takeAction = staticRoute("#task", PerformTaskLoc) ~> renderR( c => AppCircuit.wrap(_.formModel)(proxy => FormComp(proxy, c)))

    (
      staticRoute(root, DashboardLoc) ~> renderR(c => AppCircuit.wrap(_.message)(proxy => Dashboard(proxy, c)))
      | taskListRoute
      | processInstRoute
      | itemRoute
      | accRoute
      | flowRoute
      | fetchTaskRoute
      | takeAction
      ).notFound(redirectToPage(DashboardLoc)(Redirect.Replace))

  }.renderWith(layout)
  val searchingWrapper = AppCircuit.connect(_.message)

  def layout(c: RouterCtl[Loc], r: Resolution[Loc]) = {
    searchingWrapper(proxy => Main(Main.Props(c, r)))
  }

  def main(args: Array[String]): Unit = {

    val router = Router(BaseUrl.until_#, routerConfig)
    router().renderIntoDOM(dom.document.getElementById("root"))

  }

  sealed trait Loc

  case object DashboardLoc extends Loc

  case object TaskListLoc extends Loc

  case class FetchTaskLoc(id: UUID) extends Loc

  case object ProcessInstLoc extends Loc

  case object ItemLoc extends Loc

  case object AccLoc  extends Loc

  case object FlowLoc  extends Loc

  case object PerformTaskLoc extends Loc


}
