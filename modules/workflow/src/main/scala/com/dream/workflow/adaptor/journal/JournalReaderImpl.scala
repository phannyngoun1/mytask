package com.dream.workflow.adaptor.journal

import akka.NotUsed
import akka.actor.ActorSystem
import akka.persistence.jdbc.query.scaladsl.JdbcReadJournal
import akka.persistence.query.scaladsl._
import akka.persistence.query.{Offset, PersistenceQuery}
import akka.stream.scaladsl.Source
import com.dream.workflow.usecase.port.{EventBody, JournalReader}

object JournalReaderImpl {
  type ReadJournalType =
    ReadJournal with CurrentPersistenceIdsQuery with PersistenceIdsQuery with CurrentEventsByPersistenceIdQuery with EventsByPersistenceIdQuery with CurrentEventsByTagQuery with EventsByTagQuery
}

class JournalReaderImpl (system: ActorSystem) extends JournalReader {
  private val readJournal: JournalReaderImpl.ReadJournalType =
    PersistenceQuery(system).readJournalFor[JdbcReadJournal](JdbcReadJournal.Identifier)

  override def eventsByTagSource(tag: String, seqNr: Long): Source[EventBody, NotUsed] = {

    readJournal.eventsByTag(tag, Offset.sequence(seqNr)).map { ee =>
      EventBody(ee.persistenceId, ee.sequenceNr, ee.event)
    }
  }
}
