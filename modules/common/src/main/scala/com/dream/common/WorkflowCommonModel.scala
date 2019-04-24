package com.dream.common

trait BaseAction {
  def name: String
}

trait Payload


case class NonePayload() extends Payload

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

