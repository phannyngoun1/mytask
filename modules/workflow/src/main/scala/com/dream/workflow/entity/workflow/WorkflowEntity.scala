package com.dream.workflow.entity.workflow

import java.util.UUID

import akka.actor.{ActorLogging, Props}
import akka.persistence._
import cats.implicits._
import com.dream.common.EntityState
import com.dream.workflow.domain.FlowEvents.FlowCreated
import com.dream.workflow.domain._
import com.dream.workflow.entity.workflow.WorkflowProtocol._

object WorkflowEntity {

  def prop = Props(new WorkflowEntity)

  final val AggregateName = "work_flow"

  def name(uuId: UUID): String = uuId.toString

  implicit class EitherOps(val self: Either[WorkflowError, Flow]) {
    def toSomeOrThrow: Option[Flow] = self.fold(error => throw new IllegalStateException(error.message), Some(_))
  }

}

class WorkflowEntity extends PersistentActor with ActorLogging with EntityState[WorkflowError, Flow] {

  import WorkflowEntity._

  var state: Option[Flow] = None

  private def applyState(event: FlowCreated): Either[WorkflowError, Flow] =
    Either.right(
      Flow(
        event.id,
        event.name,
        event.initialActivity,
        event.flowList,
        true
      )
    )

  protected def foreachState(f: (Flow) => Unit): Unit =
    Either.fromOption(state, InvalidWorkflowStateError()).filterOrElse(_.isActive, InvalidWorkflowStateError()).foreach(f)

  override protected def mapState(f: Flow => Either[WorkflowError, Flow]): Either[WorkflowError, Flow] =
    for {
      state <- Either.fromOption(state, InvalidWorkflowStateError())
      newState <- f(state)
    } yield newState


  override def receiveRecover: Receive = {

    case SnapshotOffer(_, _state: Flow) =>
      state = Some(_state)
    case SaveSnapshotSuccess(metadata) =>
      log.info(s"receiveRecover: SaveSnapshotSuccess succeeded: $metadata")
    case SaveSnapshotFailure(metadata, reason) ⇒
      log.info(s"SaveSnapshotFailure: SaveSnapshotSuccess failed: $metadata, ${reason}")
    case event: FlowCreated =>
      log.info(s"Replay workflow event: ${event}")
      state = applyState(event).toSomeOrThrow
    case RecoveryCompleted =>
      log.info(s"Recovery completed: $persistenceId")
    case _ => log.info("Other")
  }

  override def receiveCommand: Receive = {
    case cmd: CreateWorkflowCmdRequest => persist(FlowCreated(cmd.id, cmd.name, cmd.initialActivity,  cmd.flowList)) { event =>
      state = applyState(event).toSomeOrThrow
      sender() ! CreateWorkflowCmdSuccess(event.id)
    }
    case cmd: GetWorkflowCmdRequest  => {
      foreachState { state =>
       log.info(s"Do fetching workflow ${cmd}")
        sender() ! GetWorkflowCmdSuccess(state)
      }
    }
    case cmd: GetTaskActionCmdReq =>
      foreachState{ state =>
        val actions = state.workflowList
          .find(_.activity == cmd.task.activity)
          .map(_.actionPayload(cmd.task.participantId)).get
        sender() ! GetTaskActionCmdSuccess(cmd.task.copy(actions = actions))
      }

    case cmd: GetWorkflowPayloadCmdRequest =>
      foreachState { state =>
        sender() ! GetWorkflowPayloadCmdSuccess(cmd.item.copy(initPayload = state.getInitPayload))
      }


    case SaveSnapshotSuccess(metadata) =>
      log.debug(s"receiveCommand: SaveSnapshotSuccess succeeded: $metadata")
  }

  override def persistenceId: String = s"$AggregateName-${self.path.name}"

  override protected def invalidStateError(id: Option[UUID]): WorkflowError =
    InvalidWorkflowStateError(id)
}
