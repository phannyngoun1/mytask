package com.dream.workflow.adaptor.aggregate

import java.time.Instant

import akka.NotUsed
import akka.actor.ActorRef
import akka.pattern.ask
import akka.stream.scaladsl.Flow
import akka.util.Timeout
import com.dream.common.Protocol.CmdResponseFailed
import com.dream.common.domain.ResponseError
import com.dream.workflow.domain.TaskDto
import com.dream.workflow.entity.processinstance.ProcessInstanceProtocol._
import com.dream.workflow.usecase.ProcessInstanceAggregateUseCase.Protocol
import com.dream.workflow.usecase.port.ProcessInstanceAggregateFlows
import org.sisioh.baseunits.scala.time.TimePoint

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

class ProcessInstanceAggregateFlowsImpl(aggregateRef: ActorRef) extends ProcessInstanceAggregateFlows {

  private implicit val to: Timeout = Timeout(2 seconds)

  override def createInst: Flow[CreatePInstCmdRequest, Protocol.CreatePInstCmdResponse, NotUsed] =
    Flow[CreatePInstCmdRequest]
      .mapAsync(1)(aggregateRef ? _)
      .map {
        case res: CreatePInstCmdSuccess => Protocol.CreatePInstCmdSuccess(res.id.toString)
        case  CmdResponseFailed(message) => Protocol.CreatePInstCmdFailed(ResponseError(message))
      }



  override def performTask: Flow[Protocol.PerformTaskCmdReq, Protocol.PerformTaskCmdRes, NotUsed] =
    Flow[Protocol.PerformTaskCmdReq]
    .map(req => PerformTaskCmdReq(req.pInstId, req.taskId, req.action, req.activity, req.payLoad, req.processBy))
      .mapAsync(1)(aggregateRef ? _)
    .map{
      case req:PerformTaskCmdReq =>
        Protocol.PerformTaskSuccess(req.pInstId, req.taskId, req.processBy,Instant.now())
    }

  override def getPInst: Flow[Protocol.GetPInstCmdRequest, Protocol.GetPInstCmdResponse, NotUsed] =
    Flow[Protocol.GetPInstCmdRequest]
    .map(req => GetPInstCmdRequest(req.id))
    .mapAsync(1)(aggregateRef ? _)
    .map {
      case GetPInstCmdSuccess(pInst) => Protocol.GetPInstCmdSuccess(pInst.id, pInst.flowId, pInst.folio)
      case  CmdResponseFailed(message) => Protocol.GetPInstCmdFailed(ResponseError(message))
    }

  override def getTask: Flow[Protocol.GetTaskCmdReq, Protocol.GetTaskCmdRes, NotUsed] =
    Flow[Protocol.GetTaskCmdReq]
    .map( req => GetTaskCmdReq(req.assignedTask.pInstId, req.assignedTask.taskId))
      .mapAsync(1)(aggregateRef ? _)
    .map {
      case GetTaskCmdRes(pInstId, task) => Protocol.GetTaskCmdSuccess(TaskDto(task.id, pInstId,  task.activity, task.actions))
      case CmdResponseFailed(message) => Protocol.GetTaskCmdFailed(ResponseError(message))
    }

  override def commitAction: Flow[Protocol.CommitActionCmdReq, Protocol.CommitActionCmdRes, NotUsed] = ???

  override def createNewTask: Flow[Protocol.CreateNewTaskCmdRequest, Protocol.CreateNewTaskCmdResponse, NotUsed] = ???
}
