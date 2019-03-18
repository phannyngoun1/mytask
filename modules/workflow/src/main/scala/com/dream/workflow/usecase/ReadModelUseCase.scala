package com.dream.workflow.usecase

import java.time.Instant

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.{Done, NotUsed}
import com.dream.workflow.domain.FlowEvents.{FlowCreated, FlowEvent}
import com.dream.workflow.domain.{ItemCreated, ItemEvent}
import com.dream.workflow.usecase.port.{FlowReadModelFlow, ItemReadModelFlow, JournalReader}
import org.sisioh.baseunits.scala.time.TimePoint

import scala.concurrent.{ExecutionContextExecutor, Future}

class ReadModelUseCase(
  readModelFlow: ItemReadModelFlow,
  flowReadModelFlow: FlowReadModelFlow,
  journalReader: JournalReader)(implicit val system: ActorSystem)
  extends UseCaseSupport {


  private implicit val mat: ActorMaterializer = ActorMaterializer()
  private implicit val ec: ExecutionContextExecutor = system.dispatcher

  private val projectionItemFlow: Flow[(ItemEvent, Long), Int, NotUsed] =
    Flow[(ItemEvent, Long)].flatMapConcat {
      case (event: ItemCreated, sequenceNr: Long) =>
        Source
          .single((event.id, event.name, sequenceNr, event.createdAt))
          .via(readModelFlow.newItemFlow)
    }

  private val projectionWorkflow: Flow[(FlowEvent, Long), Int, NotUsed] =
    Flow[(FlowEvent, Long)].flatMapConcat {
      case (event: FlowCreated, sequenceNr: Long) =>
        Source
          .single((event.id, event.name, sequenceNr, TimePoint.from(Instant.now())))
          .via(flowReadModelFlow.newItemFlow)
    }

  def executeItem(): Future[Done] = {
    readModelFlow.resolveLastSeqNrSource
      .flatMapConcat { lastSeqNr =>
        println(s"==============>lastSeqNr: ${lastSeqNr}")
        journalReader.eventsByTagSource(classOf[ItemEvent].getName, lastSeqNr )
      }
      .map { eventBody =>
        (eventBody.event.asInstanceOf[ItemEvent], eventBody.sequenceNr)
      }
      .via(projectionItemFlow)
      .toMat(Sink.ignore)(Keep.right)
      .run()
  }

  def executeFlow(): Future[Done] = {
    flowReadModelFlow.resolveLastSeqNrSource
      .flatMapConcat { lastSeqNr =>
        journalReader.eventsByTagSource(classOf[FlowEvent].getName, lastSeqNr )
      }
      .map { eventBody =>
        (eventBody.event.asInstanceOf[FlowEvent], eventBody.sequenceNr)
      }
      .via(projectionWorkflow)
      .toMat(Sink.ignore)(Keep.right)
      .run()
  }



}
