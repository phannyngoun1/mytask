package com.dream.workflow.usecase

import java.util.UUID

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Keep, Sink, Source, SourceQueueWithComplete}
import akka.stream._
import com.dream.common._
import com.dream.common.domain.ResponseError
import com.dream.workflow.domain.{Flow, FlowDto, TaskDto}
import com.dream.workflow.usecase.port.{FlowReadModelFlow, WorkflowAggregateFlows}

import scala.concurrent.{ExecutionContext, Future, Promise}


object WorkflowAggregateUseCase {

  object Protocol {

    sealed trait WorkflowCmdResponse

    sealed trait WorkflowCmdRequest

    case class CreateWorkflowCmdRequest(
      id: UUID,
      name: String,
      initialActivity: BaseActivity,
      workflowList: Seq[BaseActivityFlow],
    ) extends WorkflowCmdRequest

    abstract class CreateWorkflowCmdResponse() extends WorkflowCmdResponse

    case class CreateWorkflowCmdSuccess(id: UUID) extends CreateWorkflowCmdResponse

    case class CreateWorkflowCmdFailed(error: ResponseError) extends CreateWorkflowCmdResponse

    case class GetWorkflowCmdRequest(
      id: UUID
    ) extends WorkflowCmdRequest

    abstract class GetWorkflowCmdResponse() extends WorkflowCmdResponse

    case class GetWorkflowCmdSuccess(flow: Flow) extends GetWorkflowCmdResponse

    case class GetWorkflowCmdFailed(error: ResponseError) extends GetWorkflowCmdResponse

    case class GetTaskActionCmdReq(task: TaskDto) extends WorkflowCmdRequest

    trait GetTaskActionCmdRes extends  GetWorkflowCmdResponse

    case class GetTaskActionCmdSuccess(task: TaskDto) extends GetTaskActionCmdRes

    case class GetTaskActionCmdFailed(responseError: ResponseError) extends GetTaskActionCmdRes

  }

}

class WorkflowAggregateUseCase(workflow: WorkflowAggregateFlows, readSide: FlowReadModelFlow)(implicit system: ActorSystem) extends UseCaseSupport {

  import UseCaseSupport._
  import WorkflowAggregateUseCase.Protocol._

  private val bufferSize: Int = 10
  val decider: Supervision.Decider = {
    case _ => Supervision.Restart
  }

  implicit val mat = ActorMaterializer(
    ActorMaterializerSettings(system)
      .withSupervisionStrategy(decider)
  )

  def createWorkflow(request: CreateWorkflowCmdRequest)(implicit ec: ExecutionContext): Future[CreateWorkflowCmdResponse] = {
    offerToQueue(createWorkflowQueue)(request, Promise())
  }

  def getWorkflow(request: GetWorkflowCmdRequest)(implicit ec: ExecutionContext): Future[GetWorkflowCmdResponse] = {
    offerToQueue(getWorkflowQueue)(request, Promise())
  }

  private val createWorkflowQueue: SourceQueueWithComplete[(CreateWorkflowCmdRequest, Promise[CreateWorkflowCmdResponse])] =
    Source.queue[(CreateWorkflowCmdRequest, Promise[CreateWorkflowCmdResponse])](bufferSize, OverflowStrategy.dropNew)
      .via(workflow.createWorkflow.zipPromise)
      .toMat(completePromiseSink)(Keep.left)
      .run()

  private val getWorkflowQueue: SourceQueueWithComplete[(GetWorkflowCmdRequest, Promise[GetWorkflowCmdResponse])] =
    Source.queue[(GetWorkflowCmdRequest, Promise[GetWorkflowCmdResponse])](bufferSize, OverflowStrategy.dropNew)
      .via(workflow.getWorkflow.zipPromise)
      .toMat(completePromiseSink)(Keep.left)
      .run()

  def list: Future[List[FlowDto]] = {
    val sumSink =  Sink.fold[List[FlowDto], FlowDto](List.empty[FlowDto])( (m ,e) =>  e :: m )
    Source.fromPublisher(readSide.list).toMat(sumSink)(Keep.right).run()
  }
}
