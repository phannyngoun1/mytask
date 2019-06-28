package com.dream.mytask.shared.data

import java.util.UUID

object AccountData {


  case class UserJs(
    id: UUID,
    participants: Seq[UUID]
  )

  case class UserBasicInfoJs(
    name: String,
    email: Option[String]
  )

  case class AccountJson(
    id: String,
    name: String,
    currParticipantId: Option[String]
  )

  case class ParticipantJson(
    id: String,
    accountId: String,
    tasks: List[String]

  )

}
