package com.dream.mytask.modules.task

import com.dream.mytask.shared.data.Task
import diode._
import diode.data._
import autowire._
import diode.util._
import boopickle.Default._
import TaskActionListHandler.TaskListActions._

import com.dream.mytask.services.AjaxClient
import com.dream.mytask.shared.Api

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

    case class FetchTaskListAction(potResult: Pot[List[Task]] = Empty) extends PotAction[List[Task], FetchTaskListAction] {
      override def next(newResult: Pot[List[Task]]): FetchTaskListAction = FetchTaskListAction(newResult)
    }
  }
}



//class TaskListActionHandler[M](modelRW: ModelRW[M, PotMap[String, User]]) extends ActionHandler(modelRW) {
//
//
//  def loadUsers(keys: Set[String]): Future[Map[String, Pot[User]]] = ???
//
//  override protected def handle  = {
//    case action: UpdateUsers =>
//      val updateEffect = action.effect(loadUsers(action.keys))(identity)
//      action.handleWith(this, updateEffect)(AsyncAction.mapHandler(action.keys))
//  }
//}


class TaskActionListHandler[M](modelRW: ModelRW[M, Pot[List[Task]]]) extends ActionHandler(modelRW) {
  implicit val runner = new RunAfterJS
  import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  override protected def handle = {
    case action: FetchTaskListAction =>
      val updateF = action.effect(AjaxClient[Api].getTasks("8dbd6bf8-2f60-4e6e-8e3f-b374e060a940").call())(identity _)
      action.handleWith(this, updateF)(PotAction.handler())
  }
}
