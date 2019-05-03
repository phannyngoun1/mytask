package com.dream.workflow.adaptor.aggregate

import java.util.UUID

import akka.NotUsed
import akka.actor.ActorRef
import akka.pattern.ask
import akka.stream.scaladsl.Flow
import akka.util.Timeout
import com.dream.common.Protocol.{CmdResponseFailed, DefaultTaskPerformCmdResponse}
import com.dream.common.domain.ResponseError
import com.dream.workflow.domain.TaskDto
import com.dream.workflow.entity.processinstance.ProcessInstanceProtocol._
import com.dream.workflow.usecase.ProcessInstanceAggregateUseCase.Protocol
import com.dream.workflow.usecase.port.ProcessInstanceAggregateFlows

import scala.concurrent.duration._
import scala.language.postfixOps

class ProcessInstanceAggregateFlowsImpl(aggregateRef: ActorRef) extends ProcessInstanceAggregateFlows {

  private implicit val to: Timeout = Timeout(2 seconds)

  override def createInst: Flow[CreatePInstCmdRequest, Protocol.CreatePInstCmdResponse, NotUsed] =
    Flow[CreatePInstCmdRequest]
      .mapAsync(1)(aggregateRef ? _)
      .map {
        case res: CreatePInstCmdSuccess => Protocol.CreatePInstCmdSuccess(res.id.toString)
        case CmdResponseFailed(message) => Protocol.CreatePInstCmdFailed(ResponseError(message))
      }


  override def performTask: Flow[Protocol.PerformTaskCmdReq, Protocol.PerformTaskCmdRes, NotUsed] =
    Flow[Protocol.PerformTaskCmdReq]
      .mapAsync(1)(aggregateRef ? _)
      .map {
        case DefaultTaskPerformCmdResponse(activityId) => Protocol.PerformTaskSuccess(activityId)
        case CmdResponseFailed(message) => Protocol.PerformTaskFailed(ResponseError(message))
      }

  override def getPInst: Flow[Protocol.GetPInstCmdRequest, Protocol.GetPInstCmdResponse, NotUsed] =
    Flow[Protocol.GetPInstCmdRequest]
      .map(req => GetPInstCmdRequest(req.id))
      .mapAsync(1)(aggregateRef ? _)
      .map {
        case GetPInstCmdSuccess(pInst) => Protocol.GetPInstCmdSuccess(pInst.id, pInst.flowId, pInst.folio,pInst.tasks )
        case CmdResponseFailed(message) => Protocol.GetPInstCmdFailed(ResponseError(message))
      }

  override def getTask: Flow[Protocol.GetTaskCmdReq, Protocol.GetTaskCmdRes, NotUsed] =
    Flow[Protocol.GetTaskCmdReq]
      .map(req => {
        println("Protocol.GetTaskCmdReq")
        GetTaskCmdReq(req.assignedTask.pInstId, req.assignedTask.taskId, req.participantId)
      })
      .mapAsync(1)(aggregateRef ? _)
      .map {
        case GetTaskCmdRes(pInstId, participantId, task) =>
          println(s"retrieve task ${pInstId}, ${participantId}")
          Protocol.GetTaskCmdSuccess(TaskDto(task.id, pInstId, participantId, task.activity, task.actions, task.active))
        case CmdResponseFailed(message) => Protocol.GetTaskCmdFailed(ResponseError(message))
        case _ =>
          println(s"retrieve task other")
          Protocol.GetTaskCmdFailed(ResponseError(ResponseError(Some(UUID.randomUUID()), "ss")))
      }

  override def commitAction: Flow[Protocol.CommitActionCmdReq, Protocol.CommitActionCmdRes, NotUsed] =
    Flow[Protocol.CommitActionCmdReq]
      .map(req => CommitActionCmdReq(req.id, req.taskId, req.participantId, req.action, req.processAt))
      .mapAsync(1)(aggregateRef ? _)
      .map {
        case CommitActionCmdSuccess(id) => Protocol.CommitActionCmdSuccess(id)
        case CmdResponseFailed(message) => Protocol.CommitActionCmdFailed(ResponseError(message))
      }

  override def createNewTask: Flow[Protocol.CreateNewTaskCmdRequest, Protocol.CreateNewTaskCmdResponse, NotUsed] =
    Flow[Protocol.CreateNewTaskCmdRequest]
      .map(req => CreateNewTaskCmdReq(req.id, req.task, req.participantId))
      .mapAsync(1)(aggregateRef ? _)
      .map {
        case CreateNewTaskCmdSuccess(id, taskId, dest) =>
          println(s"receive CreateNewTaskCmdSuccess ${taskId}")
          Protocol.CreateNewTaskCmdSuccess(id, taskId, dest)
        case CmdResponseFailed(message) => Protocol.CreateNewTaskCmdFailed(ResponseError(message))
      }
}
