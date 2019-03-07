package com.dream.workflow.usecase.port

case class EventBody(persistenceId: String, sequenceNr: Long, event: Any)
