package com.dream.mytask.modules.task

import com.dream.mytask.shared.data.TaskItemJson
import diode._
import diode.data._
import autowire._
import diode.util._
import boopickle.Default._
import TaskActionListHandler.TaskListActions.{FetchTaskListAction, _}
import com.dream.mytask.services.AjaxClient
import com.dream.mytask.services.DataModel.RootModel
import com.dream.mytask.shared.Api
import com.dream.mytask.shared.data.WorkflowData.{PayloadJs}

import scala.util.{Failure, Try}


object TaskActionListHandler {

  case class User(id:String, name: String)

  case class UpdateUsers(
    keys: Set[String],
    state: PotState = PotState.PotEmpty,
    result: Try[Map[String, Pot[User]]] = Failure(new AsyncAction.PendingException)
  ) extends AsyncAction[Map[String, Pot[User]], UpdateUsers] {
    def next(newState: PotState, newValue: Try[Map[String, Pot[User]]]) =
      UpdateUsers(keys, newState, newValue)
  }

  // an implementation of Fetch for users
  class UserFetch(dispatch: Dispatcher) extends Fetch[String] {
    override def fetch(key: String): Unit =
      dispatch(UpdateUsers(keys = Set(key)))
    override def fetch(keys: Traversable[String]): Unit =
      dispatch(UpdateUsers(keys = Set() ++ keys))
  }

  object TaskListActions {

    case class TakeAction(
      pInstId: Option[String],
      taskId: Option[String],
      accId: Option[String],
      participantId: Option[String],
      action: Option[String],
      payLoad: Option[PayloadJs],
      potResult: Pot[String] = Empty )
      extends PotAction[String, TakeAction] {
      override def next(newResult: Pot[String]): TakeAction = TakeAction(None, None, None, None, None, None, newResult)
    }

    case class FetchTaskListAction(accId: Option[String], potResult: Pot[List[TaskItemJson]] = Empty) extends PotAction[List[TaskItemJson], FetchTaskListAction] {
      override def next(newResult: Pot[List[TaskItemJson]]): FetchTaskListAction = FetchTaskListAction(None, newResult)
    }
  }

  def apply(circuit: Circuit[RootModel]) = circuit.composeHandlers(
    new TaskActionListHandler(circuit.zoomRW(_.taskModel.taskList)((m, v) => m.copy(taskModel = m.taskModel.copy(taskList = v)))),
    new TakeActionHandler(circuit.zoomRW(_.taskModel.message)((m, v) => m.copy(taskModel = m.taskModel.copy(message = v))))
  )

}

class TakeActionHandler[M](modelRW: ModelRW[M, Pot[String]]) extends ActionHandler(modelRW) {
  implicit val runner = new RunAfterJS
  import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  override protected def handle: PartialFunction[Any, ActionResult[M]] = {
    case action: TakeAction =>
      val updateF = action.effect(AjaxClient[Api].takeAction(action.pInstId.get, action.taskId.get, action.accId.get, action.participantId.get, action.action.get, action.payLoad.get).call())(identity _)
      action.handleWith(this, updateF)(PotAction.handler())
  }
}

class TaskActionListHandler[M](modelRW: ModelRW[M, Pot[List[TaskItemJson]]]) extends ActionHandler(modelRW) {
  implicit val runner = new RunAfterJS
  import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  override protected def handle = {
    case action: FetchTaskListAction =>
      val updateF = action.effect(AjaxClient[Api].getTasks(action.accId.get).call())(identity _)
      action.handleWith(this, updateF)(PotAction.handler())

  }
}

