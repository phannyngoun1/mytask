package com.dream.mytask.shared.data

object AccountData {

  case class AccountJson(
    id: String,
    name: String
  )

  case class ParticipantJson(
    id: String,
    accountId: String
  )

}
