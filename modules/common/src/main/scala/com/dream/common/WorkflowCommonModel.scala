package com.dream.common

import java.util.UUID

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

