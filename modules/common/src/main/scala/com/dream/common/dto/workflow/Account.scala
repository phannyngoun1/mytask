package com.dream.common.dto.workflow

import java.util.UUID

object Account {

  case class AccountDto(id: UUID, name: String, fullName: String)

  case class ParticipantDto(id: UUID, accountId: UUID)

}
