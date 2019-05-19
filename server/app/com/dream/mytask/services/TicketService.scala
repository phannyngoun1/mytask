package com.dream.mytask.services

import com.dream.mytask.shared.data.AssignFormInitDataJs

import scala.concurrent.Future

trait TicketService { this: ApiService =>

  override def getTicketAssignInitData(): Future[AssignFormInitDataJs] = {
    getParticipantList().map(AssignFormInitDataJs(_))
  }

}
