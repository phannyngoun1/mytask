package com.dream.common

import java.util.UUID

import play.api.libs.json.{Format, Json}

/**
  * Action Type:
  * #COMPLETED: To complete task
  * #HANDLING: Still working on the task
  * #HANDLED: Task is still in progress but hand to another
  */
trait BaseAction {
  def name: String
  def actionType: String = "COMPLETED"
}

trait Payload


case class NonePayload() extends Payload

trait ReRoutePayload {
  def participantId: UUID
}

case class DefaultPayLoad(
  value: String
) extends Payload

trait Params

case class DefaultFlowParams(value: String) extends Params

trait BaseActivity {
  def name: String

  override def equals(obj: Any): Boolean = obj match {
    case a: BaseActivity => name.equals(a.name)
    case _ => false
  }
}

trait BaseActivityFlow {
  def activity: BaseActivity
  def contributeTypeList: List[ContributeType]
  def participants: List[UUID]
  def actionFlows: List[ActionFlow]
}

case class Activity(name: String) extends BaseActivity

object Activity {
  implicit val format: Format[Activity] = Json.format
}

case class Action(name: String, override val actionType: String) extends BaseAction

object Action {
  implicit val format: Format[Action] = Json.format
}

case class ActionFlow(action: BaseAction, activity: Option[BaseActivity])

case class ActivityFlow(
  activity: BaseActivity,
  participants: List[UUID] = List.empty ,
  contributeTypeList: List[ContributeType] = List.empty , // Direct assign - DirectAssign. Sharable, Assignable, Pickup,  Empty = any types.
  contribution: List[Contribution] = List.empty,
  actionFlows: List[ActionFlow]
) extends BaseActivityFlow

/**
  * Predefined activity flows
  */

case class StartAction() extends BaseAction {
  override val name: String = "Start"
}

case class DoneAction() extends BaseAction {
  override val name: String = "Done"
}

case class NaAction() extends BaseAction {
  override val name: String = "N/A"
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


abstract class AbstractActivityFlow() extends BaseActivityFlow {
  override def participants: List[UUID] = List.empty
  override def contributeTypeList: List[ContributeType] = List.empty
  override def actionFlows: List[ActionFlow] = List.empty
}

case class NaActivityFlow() extends AbstractActivityFlow {

  override def activity: BaseActivity = NaActivity()

}

case class DoneActivityFlow() extends AbstractActivityFlow {
  override def activity: BaseActivity = DoneActivity()
}

/**
  * @param participantId
  * @param policyList: Policy is used to reduce contribution repeated authorize configuration. Empty = None policy is accepted.
  * @param payloadAuthCode: being used to restrict payload accessible data. * = not restrict
  * @param contributeTypeList  Direct assign - DirectAssign. Sharable, Assignable, Pickup,  * = any types.
  * @param accessibleActionList: Empty = Any actions.
  */
case class Contribution(
  participantId: UUID,
  policyList: List[UUID] = List.empty,
  payloadAuthCode: String = "*",
  contributeTypeList: List[ContributeType] = List.empty,
  accessibleActionList: List[BaseAction] = List.empty
)


object Contribution {
  val directAssign = ContributeType("DirectAssign", "Direct assign")
  val sharable = ContributeType("Sharable", "Can be shared")
  val assignable = ContributeType("Assignable", "Can be assigned")
  val pickup = ContributeType("Pickup", "Pickup")
  val all = ContributeType("*", "*")
}



//Direct assign - DirectAssign. Sharable, Assignable, Pickup,  * = any types.
case class ContributeType(
  code: String,
  name: String
)

case class FlowTemplate(
  id: UUID = UUID.randomUUID(),
  name: String,
  startActivity: StartActivity = StartActivity(),
  activityFlowList: Seq[BaseActivityFlow]
)
