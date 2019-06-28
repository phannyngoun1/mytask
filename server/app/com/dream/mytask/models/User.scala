package com.dream.mytask.models

import java.util.UUID

import com.dream.mytask.shared.Common.Login
import com.dream.workflow.domain.ParticipantDto
import com.mohiva.play.silhouette.api._

case class User(
  id: UUID,
  loginInfo: LoginInfo,
  userName: Option[String],
  basicInfo: BasicInfo,
  participants: Seq[ParticipantDto],
  roles: Set[Role]
) extends Identity with Login


case class BasicInfo(
  fullName: Option[String],
  email: Option[String]
)