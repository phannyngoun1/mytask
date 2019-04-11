package com.dream.workflow.usecase

import java.time.Instant

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.{Done, NotUsed}
import com.dream.workflow.domain.Account.{AccountCreated, AccountEvent}
import com.dream.workflow.domain.FlowEvents.{FlowCreated, FlowEvent}
import com.dream.workflow.domain.Participant.{ParticipantCreated, ParticipantEvent}
import com.dream.workflow.domain.ProcessInstance.{ProcessInstanceCreated, ProcessInstanceEvent}
import com.dream.workflow.domain.{ItemCreated, ItemEvent}
import com.dream.workflow.usecase.port._
import org.sisioh.baseunits.scala.time.TimePoint

import scala.concurrent.{ExecutionContextExecutor, Future}

class ReadModelUseCase(
  readModelFlow: ItemReadModelFlow,
  flowReadModelFlow: FlowReadModelFlow,
  accountReadModelFlow: AccountReadModelFlow,
  participantReadModelFlows: ParticipantReadModelFlows,
  pInstanceReadModelFlows: PInstanceReadModelFlows,
  flagReadModelFlows: FlagReadModelFlows,
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
      case _ => Source.single("").via(Flow[String].mapAsync(1) {
        case _ => Future.successful(0)
      })
    }

  private val projectionWorkflow: Flow[(FlowEvent, Long), Int, NotUsed] =
    Flow[(FlowEvent, Long)].flatMapConcat {
      case (event: FlowCreated, sequenceNr: Long) =>
        Source
          .single((event.id, event.name, sequenceNr, TimePoint.from(Instant.now())))
          .via(flowReadModelFlow.newItemFlow)
      case _ => Source.single("").via(Flow[String].mapAsync(1) {
        case _ => Future.successful(0)
      })
    }


  private val projectionAcc: Flow[(AccountEvent, Long), Int, NotUsed] =
    Flow[(AccountEvent, Long)].flatMapConcat {
      case (event: AccountCreated, sequenceNr: Long) =>
        Source.single((event.id, event.name, event.fullName, sequenceNr, TimePoint.from(Instant.now())))
          .via(accountReadModelFlow.newAccountFlow)

      case _ => Source.single("").via(Flow[String].mapAsync(1) {
        case _ => Future.successful(0)
      })
    }

  //  teamId, departmentId, propertyId

  private val projectionParticipant: Flow[(ParticipantEvent, Long), Int, NotUsed] =
    Flow[(ParticipantEvent, Long)].flatMapConcat {
      case (event: ParticipantCreated, sequenceNr: Long) =>
        Source.single((event.id, event.accountId, event.teamId, event.departmentId, event.propertyId, sequenceNr, TimePoint.from(Instant.now())))
          .via(participantReadModelFlows.newItemFlow)
      case _ => Source.single("").via(Flow[String].mapAsync(1) {
        case _ => Future.successful(0)
      })
    }


  private val projectPInstance: Flow[(ProcessInstanceEvent, Long), Int, NotUsed] =
    Flow[(ProcessInstanceEvent, Long)].flatMapConcat {
      case (event: ProcessInstanceCreated, seq: Long) =>
        Source.single((event.id, event.folio, seq, TimePoint.from(Instant.now())))
          .via(pInstanceReadModelFlows.newPInst)
      case _ => Source.single("").via(Flow[String].mapAsync(1) {
        case _ => Future.successful(0)
      })
    }

  private val project: Flow[EventBody, Long, NotUsed] =
    Flow[EventBody].flatMapConcat {
      case event: EventBody =>
        event.event match {
          case ev: ProcessInstanceCreated =>
            Source
              .single((ev.id, ev.folio, 1L, TimePoint.from(Instant.now())))
              .via(pInstanceReadModelFlows.newPInst).map(_ => event.sequenceNr)
          case ev: ParticipantCreated =>
            Source.single((ev.id, ev.accountId, ev.teamId, ev.departmentId, ev.propertyId, 1L, TimePoint.from(Instant.now())))
              .via(participantReadModelFlows.newItemFlow).map(_ => event.sequenceNr)
          case ev: AccountCreated =>
            Source.single((ev.id, ev.name, ev.fullName, 1L, TimePoint.from(Instant.now())))
              .via(accountReadModelFlow.newAccountFlow).map(_ => event.sequenceNr)
          case ev: FlowCreated =>
            Source
              .single((ev.id, ev.name, 1L, TimePoint.from(Instant.now())))
              .via(flowReadModelFlow.newItemFlow).map(_ => event.sequenceNr)
          case ev: ItemCreated =>
            Source
              .single((ev.id, ev.name, 1L, ev.createdAt))
              .via(readModelFlow.newItemFlow).map(_ => event.sequenceNr)
          case _ =>
            println(s"event no handler ${event} ")
            Source.single("").via(Flow[String].mapAsync(1) {
              case _ => Future.successful(event.sequenceNr)
            })
        }
    }


  def executeItem: Future[Done] = {
    readModelFlow.resolveLastSeqNrSource
      .flatMapConcat { lastSeqNr =>
        println(s"==============>lastSeqNr: ${lastSeqNr}")
        journalReader.eventsByTagSource(classOf[ItemEvent].getName, lastSeqNr)
      }
      .map { eventBody =>
        (eventBody.event.asInstanceOf[ItemEvent], eventBody.sequenceNr)
      }
      .via(projectionItemFlow)
      .toMat(Sink.ignore)(Keep.right)
      .run()
  }

  def executeFlow: Future[Done] = {
    flowReadModelFlow.resolveLastSeqNrSource
      .flatMapConcat { lastSeqNr =>
        journalReader.eventsByTagSource(classOf[FlowEvent].getName, lastSeqNr)
      }
      .map { eventBody =>
        (eventBody.event.asInstanceOf[FlowEvent], eventBody.sequenceNr)
      }
      .via(projectionWorkflow)
      .toMat(Sink.ignore)(Keep.right)
      .run()
  }


  def executeAcc: Future[Done] = {
    accountReadModelFlow.resolveLastSeqNrSource
      .flatMapConcat { lastSeqNr =>

        println(s"Account==============>lastSeqNr: ${lastSeqNr}")

        journalReader.eventsByTagSource(classOf[AccountEvent].getName, lastSeqNr)
      }
      .map { eventBody =>
        (eventBody.event.asInstanceOf[AccountEvent], eventBody.sequenceNr)
      }
      .via(projectionAcc)

      .toMat(Sink.ignore)(Keep.right)
      .run()
  }

  def executeParticipant: Future[Done] = {
    participantReadModelFlows.resolveLastSeqNrSource
      .flatMapConcat { lastSeqNr =>
        journalReader.eventsByTagSource(classOf[ParticipantEvent].getName, lastSeqNr)
      }
      .map { eventBody =>
        (eventBody.event.asInstanceOf[ParticipantEvent], eventBody.sequenceNr)
      }
      .via(projectionParticipant)
      .toMat(Sink.ignore)(Keep.right)
      .run()
  }

  def execute: Future[Done] = {
    flagReadModelFlows.resolveLastSeqNrSource
      .flatMapConcat(lastSeqNr => {
        println(s"last seq nr ${lastSeqNr}")
        journalReader.eventsByTagSource("workflow", lastSeqNr)
      }).via(project)
      .map( ("workflow", _))
      .via(flagReadModelFlows.update)
      .toMat(Sink.ignore)(Keep.right)
      .run()
  }

  def executePInst: Future[Done] = {
    pInstanceReadModelFlows.resolveLastSeqNrSource
      .flatMapConcat { lastSeqNr =>

        println(s"read side lastSeqNr: ${lastSeqNr}")
        journalReader.eventsByTagSource(classOf[ProcessInstanceEvent].getName, lastSeqNr)
      }
      .map { eventBody =>

        println(s"read side p.inst: ${eventBody}")

        (eventBody.event.asInstanceOf[ProcessInstanceEvent], eventBody.sequenceNr)
      }
      .via(projectPInstance)
      .toMat(Sink.ignore)(Keep.right)
      .run()
  }

  //  processInstanceAggregateFlows
}
