package com.dream.ticket.flow

import com.dream.common._

object TicketFlowTemplate{

  val ticketActivity = Activity("Ticketing")

  val startActionFlow = ActionFlow(
    action = StartAction(),
    activity = Some(ticketActivity)
  )

  val startActivityFlow = ActivityFlow(
    activity = StartActivity(),
    actionFlows = List(startActionFlow)
  )


  val editTicketActionFlow = ActionFlow(
    action = Action("Edit", "HANDLING"),
    None
  )

  val closeTicketActionFlow = ActionFlow(
    action = Action("Close", "COMPLETED"),
    activity = Some(DoneActivity())
  )

  val assignTicketActionFlow = ActionFlow(
    action = Action("Assign", "HANDLED"),
    activity = None
  )

  val addCommentActionFlow = ActionFlow(
    action = Action("Comment", "HANDLING"),
    activity = None
  )

  val ticketActivityFlow = ActivityFlow(

    activity = ticketActivity,
    contributeTypeList = List( "DirectAssign", "Sharable", "Assignable", "Pickup" , "*" ),
    actionFlows = List(
      editTicketActionFlow,
      closeTicketActionFlow,
      assignTicketActionFlow,
      addCommentActionFlow
    )
  )

  val activityFlowList: Seq[BaseActivityFlow] = Seq(
    startActivityFlow,
    ticketActivityFlow,

  )

  private def ticketFlowTemplate = FlowTemplate(
    name = "Ticket",
    activityFlowList = activityFlowList
  )
  def apply(): FlowTemplate =ticketFlowTemplate



}

