package com.dream.mytask.shared

import com.dream.mytask.shared.data.AssignFormInitDataJs

import scala.concurrent.Future

trait TicketApi {

  def getTicketAssignInitData() : Future[AssignFormInitDataJs]

}
