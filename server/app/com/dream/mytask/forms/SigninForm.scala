package com.dream.mytask.forms

import play.api.data.Form
import play.api.data.Forms._

object SigninForm {
  val form = Form(
    mapping(
      "userName" -> text,
      "password" -> nonEmptyText,
      "rememberMe" -> boolean
    )(Data.apply)(Data.unapply)
  )

  case class Data(
    userName: String,
    password: String,
    rememberMe: Boolean
  )

}
