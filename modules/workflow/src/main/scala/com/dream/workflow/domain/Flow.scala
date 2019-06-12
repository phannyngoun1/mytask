package com.dream.workflow.domain

import java.time.Instant
import java.util.UUID

import com.dream.common._
import com.dream.common.domain.ErrorMessage
import org.sisioh.baseunits.scala.time.TimePoint

sealed trait WorkflowError extends ErrorMessage

case class DefaultWorkflowError(message: String) extends WorkflowError

case class ActivityNotFoundError(message: String) extends WorkflowError

case class InvalidWorkflowStateError(override val id: Option[UUID] = None ) extends WorkflowError {
  override val message: String = s"Invalid state${id.fold("")(id => s":id = ${id.toString}")}"
}

case object TestActivity extends BaseActivity {
  override def name: String = "test"
}

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
      currAct <- findCurrentActivity(onActivity)
      nextAct <- nextActivity(by, action)(currAct)
    } yield nextAct

  }

    def findCurrentActivity(activity: BaseActivity) : Either[WorkflowError, BaseActivityFlow] = {

      println("findCurrentActivity")

      workflowList.find(_.activity == activity ) match {
        case None => Left(ActivityNotFoundError(s"Current activity: ${activity.name} can't be found"))
        case Some(act: BaseActivityFlow )=> Right(act)
      }
    }



  private def nextActivity(participantAccess: ParticipantAccess, action: BaseAction)(currActivityFlow: BaseActivityFlow): Either[WorkflowError, BaseActivityFlow] = {
    currActivityFlow match {
      case act: ActivityFlow =>
        act.actionFlows.find(_.action == action) match {

          case Some(ActionFlow(_, None)) =>
            Right(NaActivityFlow())
          case Some(ActionFlow(_, Some(Activity("Done")))) =>
            Right(DoneActivityFlow())
          case Some(af: ActionFlow) =>
            workflowList.find(_.activity == af.activity.getOrElse(None)) match {
              case Some(value) => Right(value)
              case _  => Left(ActivityNotFoundError(s"1.Next activity cannot found by action: ${action.name}; current activity ${currActivityFlow.activity.name}"))
          }
        case _ => Left(ActivityNotFoundError(s"2.Next activity cannot found by action: ${action.name}; current activity ${currActivityFlow.activity.name}"))
      }
      case  _ => Left(ActivityNotFoundError(""))
    }
  }

}




