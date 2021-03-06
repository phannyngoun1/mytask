package com.dream.workflow.usecase

import java.util.UUID

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Keep, Sink, Source, SourceQueueWithComplete}
import akka.stream._
import com.dream.common.domain.ResponseError
import com.dream.workflow.domain.Item
import com.dream.workflow.usecase.WorkflowAggregateUseCase.Protocol.{GetWorkflowPayloadCmdRequest, GetWorkflowPayloadCmdSuccess}
import com.dream.workflow.usecase.port.{ItemAggregateFlows, ItemReadModelFlow, WorkflowAggregateFlows}

import scala.concurrent.{ExecutionContext, Future, Promise}

object ItemAggregateUseCase {

  object Protocol {

    sealed trait ItemCmdResponse

    sealed trait ItemCmdRequest

    case class CreateItemCmdRequest(
      id: UUID,
      name: String,
      desc: Option[String],
      workflowId: UUID
    ) extends ItemCmdRequest

    abstract class CreateItemCmdResponse extends ItemCmdResponse

    case class CreateItemCmdSuccess(id: UUID) extends CreateItemCmdResponse

    case class CreateItemCmdFailed(error: ResponseError) extends CreateItemCmdResponse

    case class GetItemCmdRequest(
      id: UUID
    ) extends ItemCmdRequest

    abstract class GetItemCmdResponse extends ItemCmdResponse

    case class GetItemCmdSuccess(
      id: UUID,
      name: String,
      desc: Option[String],
      workflowId: UUID
    ) extends GetItemCmdResponse

    case class GetItemCmdFailed(error: ResponseError) extends GetItemCmdResponse

    case class GetWorkflowIdCmdRequest(itemId: UUID) extends ItemCmdRequest

    abstract class GetWorkflowIdCmdResponse extends ItemCmdResponse

    case class GetWorkflowIdCmdSuccess(workflowId: UUID) extends GetWorkflowIdCmdResponse

    case class GetWorkflowIdCmdFailed(error: ResponseError) extends GetWorkflowIdCmdResponse
  }
}

class ItemAggregateUseCase(
  itemAggregateFlows: ItemAggregateFlows,
  workflowAggregateFlows: WorkflowAggregateFlows,
  itemReadModelFlow: ItemReadModelFlow
)(implicit system: ActorSystem) extends UseCaseSupport {

  import ItemAggregateUseCase.Protocol._
  import UseCaseSupport._

  val decider: Supervision.Decider = {
    case _ => Supervision.Restart
  }

  implicit val mat = ActorMaterializer(
    ActorMaterializerSettings(system)
      .withSupervisionStrategy(decider)
  )
  //private implicit val ec: ExecutionContextExecutor = system.dispatcher

  private val bufferSize: Int = 10

  def createItem(request: CreateItemCmdRequest)(implicit ec: ExecutionContext): Future[CreateItemCmdResponse] = {
    offerToQueue(createItemQueue)(request, Promise())
  }

  def getItem(request: GetItemCmdRequest)(implicit ec: ExecutionContext): Future[GetItemCmdResponse] = {
    offerToQueue(getItemQueue)(request, Promise())
  }


  def list(implicit ec: ExecutionContext): Future[List[Item]] = {

    val sumSink =  Sink.fold[List[Item], Item](List.empty[Item])( (m ,e) =>  e :: m )
    Source.fromPublisher(itemReadModelFlow.list)
      .map(GetWorkflowPayloadCmdRequest)
      .via(workflowAggregateFlows.getWorkflowPayloadFlow.map {
        case GetWorkflowPayloadCmdSuccess(item) => {
          println(s"getWorkflowPayloadFlow ${item}")
          item
        }
      })
      .toMat(sumSink)(Keep.right).run()
  }

  private val createItemQueue: SourceQueueWithComplete[(CreateItemCmdRequest, Promise[CreateItemCmdResponse])] =
    Source.queue[(CreateItemCmdRequest, Promise[CreateItemCmdResponse])](bufferSize, OverflowStrategy.dropNew)
      .via(itemAggregateFlows.createItem.zipPromise)
      .toMat(completePromiseSink)(Keep.left)
      .run()

  private val getItemQueue: SourceQueueWithComplete[(GetItemCmdRequest, Promise[GetItemCmdResponse])] =
    Source.queue[(GetItemCmdRequest, Promise[GetItemCmdResponse])](bufferSize, OverflowStrategy.dropNew)
      .via(itemAggregateFlows.getItem.zipPromise)
      .toMat(completePromiseSink)(Keep.left)
      .run()




}
