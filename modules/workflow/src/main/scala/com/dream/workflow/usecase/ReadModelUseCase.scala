package com.dream.workflow.usecase

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.{Done, NotUsed}
import com.dream.workflow.domain.{ItemCreated, ItemEvent}
import com.dream.workflow.usecase.port.{ItemReadModelFlow, JournalReader}

import scala.concurrent.{ExecutionContextExecutor, Future }

class ReadModelUseCase(readModelFlow: ItemReadModelFlow, journalReader: JournalReader)(implicit val system: ActorSystem)
  extends UseCaseSupport {


  private implicit val mat: ActorMaterializer = ActorMaterializer()
  private implicit val ec: ExecutionContextExecutor = system.dispatcher

  private val projectionFlow: Flow[(ItemEvent, Long), Int, NotUsed] =
    Flow[(ItemEvent, Long)].flatMapConcat {
      case (event: ItemCreated, sequenceNr: Long) =>
        Source
          .single((event.id, event.name, sequenceNr, event.createdAt))
          .via(readModelFlow.newItemFlow)
    }

  def execute(): Future[Done] = {
    readModelFlow.resolveLastSeqNrSource
      .flatMapConcat { lastSeqNr =>
        println(s"==============>lastSeqNr: ${lastSeqNr}")
        journalReader.eventsByTagSource(classOf[ItemEvent].getName, lastSeqNr )
      }
      .map { eventBody =>
        (eventBody.event.asInstanceOf[ItemEvent], eventBody.sequenceNr)
      }
      .via(projectionFlow)
      .toMat(Sink.ignore)(Keep.right)
      .run()
  }

}
