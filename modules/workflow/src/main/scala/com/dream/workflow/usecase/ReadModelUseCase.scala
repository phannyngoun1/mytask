package com.dream.workflow.usecase

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.{Done, NotUsed}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import com.dream.workflow.domain.{ItemCreated, ItemEvent}
import com.dream.workflow.usecase.port.{JournalReader, ReadModelFlow}

import scala.concurrent.{ExecutionContextExecutor, Future}

class ReadModelUseCase(readModelFlow: ReadModelFlow, journalReader: JournalReader)
  (implicit val system: ActorSystem) {

  private implicit val mat: ActorMaterializer = ActorMaterializer()
  private implicit val ec: ExecutionContextExecutor = system.dispatcher

  private val projectionFlow: Flow[(ItemEvent, Long), Int, NotUsed] =
    Flow[(ItemEvent, Long)].flatMapConcat {
      case (event: ItemCreated, sequenceNr: Long) =>
        Source
        .single((event.id, event.name, sequenceNr))
        .via(readModelFlow.newItemFlow)
    }

  def execute(): Future[Done] = {
    readModelFlow.resolveLastSeqNrSource
      .flatMapConcat { lastSeqNr =>
        journalReader.eventsByTagSource(classOf[ItemEvent].getName, lastSeqNr + 1)
      }
      .map { eventBody =>
        (eventBody.event.asInstanceOf[ItemEvent], eventBody.sequenceNr)
      }
      .via(projectionFlow)
      .toMat(Sink.ignore)(Keep.right)
      .run()
  }

}
