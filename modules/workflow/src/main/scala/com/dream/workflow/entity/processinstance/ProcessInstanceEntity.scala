package com.dream.workflow.entity.processinstance

import java.util.UUID

import akka.actor.{ActorLogging, Props}
import akka.persistence._
import cats.implicits._
import com.dream.common.EntityState
import com.dream.common.Protocol.CmdResponseFailed
import com.dream.common.domain.ResponseError
import com.dream.workflow.domain.ProcessInstance.{InstError, InvalidInstStateError, ProcessInstanceCreated}
import com.dream.workflow.domain._
import com.dream.workflow.entity.processinstance.ProcessInstanceProtocol._

object ProcessInstanceEntity {

  final val AggregateName = "p-inst"

  def prop = Props(new ProcessInstanceEntity)

  def name(uuId: UUID): String = uuId.toString

  implicit class EitherOps(val self: Either[InstError, ProcessInstance]) {
    def toSomeOrThrow: Option[ProcessInstance] = self.fold(error => throw new IllegalStateException(error.message), Some(_))
  }

}

class ProcessInstanceEntity extends PersistentActor
  with ActorLogging
  with EntityState[InstError, ProcessInstance] {

  import ProcessInstanceEntity._

  private var state: Option[ProcessInstance] = None

  override def receiveRecover: Receive = {
    case SnapshotOffer(_, _state: ProcessInstance) =>
      println(s"SnapshotOffer ${_state}")
      state = Some(_state)

    case SaveSnapshotSuccess(metadata) =>
      log.debug(s"receiveRecover: SaveSnapshotSuccess succeeded: $metadata")
    case SaveSnapshotFailure(metadata, reason) ⇒
      log.debug(s"SaveSnapshotFailure: SaveSnapshotSuccess failed: $metadata, ${reason}")
    case event: ProcessInstanceCreated =>
      println(s"replay event: $event")
      state = applyState(event).toSomeOrThrow
    case RecoveryCompleted =>
      println(s"Recovery completed: $persistenceId")
    case _ => log.debug("Other")
  }

  override def persistenceId: String = s"$AggregateName-${self.path.name}"

  override def receiveCommand: Receive = {

    case cmd: CreatePInstCmdRequest => persist(ProcessInstanceCreated(
      id = cmd.id,
      flowId = cmd.flowId,
      folio = cmd.folio,
      contentType = cmd.contentType,
      createdBy = cmd.createdBy,
      description = cmd.description,
      task = cmd.task
    )) { event =>
      state = applyState(event).toSomeOrThrow
      sender() ! CreatePInstCmdSuccess(event.id)
    }

    case GetPInstCmdRequest(id) if equalsId(id)(state, _.id.equals(id)) =>
      foreachState { state =>
        sender() ! GetPInstCmdSuccess(state)
      }
    case GetTaskCmdReq(id, taskId) if equalsId(id)(state, _.id.equals(id)) =>
      foreachState { state =>
        state.tasks.find(_.id.equals(taskId)) match {
          case Some(task) => sender() ! GetTaskCmdRes(task)
          case None => sender() ! CmdResponseFailed(ResponseError(Some(id), s"Task id: ${taskId} not found"))
        }
      }
    case SaveSnapshotSuccess(metadata) =>
      log.debug(s"receiveCommand: SaveSnapshotSuccess succeeded: $metadata")
  }

  override protected def foreachState(f: ProcessInstance => Unit): Unit =
    Either.fromOption(state, InvalidInstStateError()).filterOrElse(_.isActive, InvalidInstStateError()).foreach(f)

  private def applyState(event: ProcessInstanceCreated): Either[InstError, ProcessInstance] =
    Either.right(
      ProcessInstance(
        id = event.id,
        createdBy = event.createdBy,
        flowId = event.flowId,
        folio = event.folio,
        contentType = event.contentType,
        description = event.description,
        tasks = List(event.task)
      )
    )

  override protected def mapState(f: ProcessInstance => Either[InstError, ProcessInstance]): Either[InstError, ProcessInstance] =
    for {
      state <- Either.fromOption(state, InvalidInstStateError())
      newState <- f(state)
    } yield newState

  override protected def invaliStateError(id: Option[UUID]): InstError =  InvalidInstStateError(id)
}
