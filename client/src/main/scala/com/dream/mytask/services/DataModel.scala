package com.dream.mytask.services

import diode.data.Pot

object DataModel {
  case class RootModel(
    message: Pot[String]
  )
}
