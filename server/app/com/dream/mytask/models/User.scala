package com.dream.mytask.models

import com.dream.mytask.shared.Common.Login
import com.mohiva.play.silhouette.api._

case class User(
  id: Int,
  loginInfo: LoginInfo,
  userName: Option[String],
  basicInfo: BasicInfo,
  roles: Set[Role]
) extends Identity with Login


case class BasicInfo(
  fullName: Option[String],
  email: Option[String]
)