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
    participants = List.empty,
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
    participants = List.empty, // to be replaced with participants
    contributeTypeList = List(Contribution.directAssign, Contribution.assignable, Contribution.sharable, Contribution.pickup, Contribution.all ),
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

