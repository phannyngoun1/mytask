package com.dream.workflow.domain

import java.time.Instant
import java.util.UUID

import com.dream.common.{BaseAction, BaseActivity, Params}
import com.dream.common.domain.ErrorMessage
import org.sisioh.baseunits.scala.time.TimePoint
import play.api.libs.json._

sealed trait WorkflowError extends ErrorMessage

case class DefaultWorkflowError(message: String) extends WorkflowError

case class ActivityNotFoundError(message: String) extends WorkflowError

case class InvalidWorkflowStateError(override val id: Option[UUID] = None ) extends WorkflowError {
  override val message: String = s"Invalid state${id.fold("")(id => s":id = ${id.toString}")}"
}


case object TestActivity extends BaseActivity {
  override def name: String = "test"
}


sealed trait BaseActivityFlow {
  def activity: BaseActivity
  def participants: List[UUID]
  def actionFlows: List[ActionFlow]
}

//object BaseActivityFlow {
//  implicit val jsonFormat: OFormat[BaseActivityFlow] = derived.oformat[BaseActivityFlow]()
//}


case class ActionHis(
  participantId: UUID,
  action: BaseAction
)


case class ActivityHis(
  id: UUID,
  activity: BaseActivity,
  actionHis: Seq[ActionHis],
  description: String,
  actionDate: Instant = Instant.now()
)


case class ActionFlow(action: BaseAction, activity: BaseActivity )

case class ActivityFlow(activity: BaseActivity, participants: List[UUID], actionFlows: List[ActionFlow]) extends BaseActivityFlow

case class StartAction() extends BaseAction {
  override val name: String = "Start"
}


case class DoneAction() extends BaseAction {
  override val name: String = "Done"
}

case class StartActivity() extends BaseActivity() {
  override val name: String = "Start"
}

case class CurrActivity() extends BaseActivity {
  override val name: String = "StayStill"
}


case class NaActivity() extends BaseActivity {
  override val name: String = "NaActivity"
}

case class DoneActivity() extends BaseActivity {
  override val name: String = "Done"
}

/**
  * Predefined activity flows
  */

abstract class AbstractActivityFlow() extends BaseActivityFlow {
  override def participants: List[UUID] = List.empty

  override def actionFlows: List[ActionFlow] = List.empty
}
case class NaActivityFlow() extends AbstractActivityFlow {

  override def activity: BaseActivity = NaActivity()

}

case class StayStillActivityFlow() extends AbstractActivityFlow {
  override def activity: BaseActivity = CurrActivity()
}


case class DoneActivityFlow() extends AbstractActivityFlow {
  override def activity: BaseActivity = DoneActivity()
}

case class Activity(name: String) extends BaseActivity

object Activity {
  implicit val format: Format[Activity] = Json.format
}



case class Action(name: String) extends BaseAction

object Action {
  implicit val format: Format[Action] = Json.format
}


case class DoAction(
  instanceId: Option[UUID] = None,

  by: Participant,
  params: Option[Params] = None
)


//object DoAction {
//  implicit val format: Format[DoAction] = Json.format
//}

//object WorkFlow {
//  implicit val format: Format[Flow] = Json.format
//}

case class FlowDto(
  id: UUID,
  name: String,
  createdAt: TimePoint,
  updatedAt: TimePoint,
  isActive: Boolean = true
)

case class Flow(
  id: UUID,
  name: String,
  initialActivity: BaseActivity,
  workflowList: Seq[BaseActivityFlow],
  isActive: Boolean = true

) {


  /**based one current activity + action + authorized participant => Next Activity flow
    */
  //TODO: check for authorized participant

  def nextActivity(action: BaseAction, onActivity: BaseActivity, by: ParticipantAccess, noneParticipantAllowed: Boolean = false): Either[WorkflowError, BaseActivityFlow ] = {

    for {
      currAct <- checkCurrentActivity(onActivity)
      nextAct <- nextActivity(by, action)(currAct)
    } yield nextAct

  }

  private def checkCurrentActivity(activity: BaseActivity) : Either[WorkflowError, BaseActivityFlow] =

    workflowList.find(_.activity == activity ) match {
      case None => Left(ActivityNotFoundError(s"Current activity: ${activity.name} can't be found"))
      case Some(act: BaseActivityFlow )=> Right(act)
    }

  private def nextActivity(participantAccess: ParticipantAccess, action: BaseAction)(currActivityFlow: BaseActivityFlow): Either[WorkflowError, BaseActivityFlow] = {
    currActivityFlow match {
      case act: ActivityFlow =>
        act.actionFlows.find(_.action == action) match {
          case Some(ActionFlow(_, _: CurrActivity)) => Right(currActivityFlow)
        case Some(af: ActionFlow) => workflowList.find(_.activity == af.activity) match {
          case Some(value) => Right(value)
          case _  => Left(ActivityNotFoundError(s"1.Next activity cannot found by action: ${action.name}; current activity ${currActivityFlow.activity.name}"))
        }
        case _ => Left(ActivityNotFoundError(s"2.Next activity cannot found by action: ${action.name}; current activity ${currActivityFlow.activity.name}"))
      }
      case act: StayStillActivityFlow => Right(act)
      case  _ => Left(ActivityNotFoundError(""))
    }
  }

}




