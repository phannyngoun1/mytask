package com.dream.mytask.shared.data

object AccountData {

  case class AccountJson(
    id: String,
    name: String,
    currParticipantId: Option[String]
  )

  case class ParticipantJson(
    id: String,
    accountId: String
  )

}
