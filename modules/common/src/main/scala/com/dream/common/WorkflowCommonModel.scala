package com.dream.common

import julienrf.json.derived
import play.api.libs.json.OFormat



trait BaseAction {
  def name: String
}

sealed trait PayLoad {

}

object PayLoad {
  implicit val jsonFormat: OFormat[PayLoad] = derived.oformat[PayLoad]()
}

case class DefaultPayLoad(
  value: String
) extends PayLoad

trait Params

case class DefaultFlowParams(value: String) extends Params

trait BaseActivity {
  def name: String


  override def equals(obj: Any): Boolean = obj match {
    case a: BaseActivity => name.equals(a.name)
    case _ => false
  }
}

