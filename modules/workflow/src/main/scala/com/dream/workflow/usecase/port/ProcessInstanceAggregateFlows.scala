package com.dream.workflow.usecase.port

import akka.NotUsed
import akka.stream.scaladsl.Flow
import com.dream.workflow.entity.processinstance.ProcessInstanceProtocol.CreatePInstCmdRequest
import com.dream.workflow.usecase.ProcessInstanceAggregateUseCase.Protocol

trait ProcessInstanceAggregateFlows {

  def createInst: Flow[CreatePInstCmdRequest, Protocol.CreatePInstCmdResponse, NotUsed]

  def getPInst: Flow[Protocol.GetPInstCmdRequest, Protocol.GetPInstCmdResponse, NotUsed]

  def commitAction:  Flow[Protocol.CommitActionCmdReq, Protocol.CommitActionCmdRes, NotUsed]

  def createNewTask: Flow[Protocol.CreateNewTaskCmdRequest, Protocol.CreateNewTaskCmdResponse, NotUsed]

  def performTask: Flow[Protocol.PerformTaskCmdReq, Protocol.PerformTaskCmdRes, NotUsed]

  def getTask: Flow[Protocol.GetTaskCmdReq, Protocol.GetTaskCmdRes, NotUsed]


}
