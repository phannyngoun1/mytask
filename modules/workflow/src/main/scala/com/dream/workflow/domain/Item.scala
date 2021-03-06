package com.dream.workflow.domain

import java.time.Instant
import java.util.UUID

import com.dream.common.domain.ErrorMessage
import org.sisioh.baseunits.scala.time.TimePoint

object Item {

  sealed trait ItemError extends ErrorMessage

  case class DefaultItemError(message: String) extends ItemError

  case class InvalidItemStateError(override val id: Option[UUID] = None) extends ItemError {
    override val message: String = s"Invalid state${id.fold("")(id => s":id = ${id.toString}")}"
  }

  case class ItemDto(

    id: UUID,
    name: String,
    desc: Option[String],
    workflowId: UUID,

  )
}

case class Item(
  id: UUID,
  name: String,
  desc: Option[String],
  workflowId: UUID,
  initPayload: Option[String] =  None,
  isActive: Boolean = true
)

sealed trait ItemEvent

case class ItemCreated(
  id: UUID,
  name: String,
  desc: Option[String],
  workflowId: UUID,
  createdAt: TimePoint = TimePoint.from(Instant.now())
) extends ItemEvent
