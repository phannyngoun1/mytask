package com.dream.workflow.adaptor.aggregate

import akka.NotUsed
import akka.actor.ActorRef
import akka.pattern.ask
import akka.stream.scaladsl.Flow
import akka.util.Timeout
import com.dream.common.Protocol.CmdResponseFailed
import com.dream.common.domain.ResponseError
import com.dream.workflow.entity.workflow.WorkflowProtocol._
import com.dream.workflow.usecase.WorkflowAggregateUseCase.Protocol
import com.dream.workflow.usecase.port.WorkflowAggregateFlows

import scala.concurrent.duration._
import scala.language.postfixOps

class WorkflowAggregateFlowsImpl(aggregateRef: ActorRef) extends WorkflowAggregateFlows {

  private implicit val to: Timeout = Timeout(2 seconds)

  override def createWorkflow: Flow[Protocol.CreateWorkflowCmdRequest, Protocol.CreateWorkflowCmdResponse, NotUsed] =
    Flow[Protocol.CreateWorkflowCmdRequest]
      .map(req => CreateWorkflowCmdRequest(req.id, req.name, req.initialActivity,  req.workflowList))
      .mapAsync(1)(aggregateRef ? _)
      .map {
        case res: CreateWorkflowCmdSuccess => Protocol.CreateWorkflowCmdSuccess(res.id)
        case CmdResponseFailed(message) => Protocol.CreateWorkflowCmdFailed(ResponseError(message))
      }

  override def getWorkflow: Flow[Protocol.GetWorkflowCmdRequest, Protocol.GetWorkflowCmdResponse, NotUsed] =
    Flow[Protocol.GetWorkflowCmdRequest]
      .map(req => GetWorkflowCmdRequest(req.id))
      .mapAsync(1)(aggregateRef ? _)
      .map {
        case res: GetWorkflowCmdSuccess => {
          println(s"Workflow fetched ${res.workflow}")
          Protocol.GetWorkflowCmdSuccess(res.workflow)
        }
        case CmdResponseFailed(message) => {
          println("Failed to fetch workflow")
          Protocol.GetWorkflowCmdFailed(ResponseError(message))
        }
        case _ => {
          println("unhandled fetch item")
          throw  new RuntimeException("unhandled fetch workflow")
        }
      }

  override def getTaskActions: Flow[Protocol.GetTaskActionCmdReq, Protocol.GetTaskActionCmdRes, NotUsed] =
    Flow[Protocol.GetTaskActionCmdReq]
      .map(req => GetTaskActionCmdReq(req.task.flowId, req.task))
      .mapAsync(1)(aggregateRef ? _)
      .map {
        case res: GetTaskActionCmdSuccess => {
          println(s"Workflow fetched ${res}")
          Protocol.GetTaskActionCmdSuccess(res.task)
        }
        case CmdResponseFailed(message) => {
          println("Failed to get TaskActions")
          Protocol.GetTaskActionCmdFailed(ResponseError(message))
        }
        case _ => {
          println("unhandled getTaskActions")
          throw  new RuntimeException("unhandled getTaskActions")
        }
      }

  override def getWorkflowPayloadFlow: Flow[Protocol.GetWorkflowPayloadCmdRequest, Protocol.GetWorkflowPayloadCmdResponse, NotUsed] =
    Flow[Protocol.GetWorkflowPayloadCmdRequest]
      .map(req => GetWorkflowPayloadCmdRequest(req.item.workflowId, req.item))
      .mapAsync(1)(aggregateRef ? _)
      .map {
        case res: GetWorkflowPayloadCmdSuccess => {
          println(s"Workflow payload fetched ${res}")
          Protocol.GetWorkflowPayloadCmdSuccess(res.item)
        }
        case CmdResponseFailed(message) => {
          println("Failed to get Workflow payload")
          Protocol.GetWorkflowPayloadCmdFailed(ResponseError(message))
        }
        case _ => {
          println("unhandled Workflow payload ")
          throw new RuntimeException("unhandled getTaskActions")
        }
      }
}
