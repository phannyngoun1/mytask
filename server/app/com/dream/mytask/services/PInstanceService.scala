package com.dream.mytask.services

import java.util.UUID

import com.dream.mytask.shared.data.ProcessInstanceData._
import com.dream.mytask.shared.data.{ActionInfoJs, ActionItemJson, TaskDestinationJs, TaskInfoJs}
import com.dream.workflow.usecase.ProcessInstanceAggregateUseCase.Protocol.{CreatePInstCmdRequest, CreatePInstCmdSuccess, GetPInstCmdRequest, GetPInstCmdSuccess}

import scala.concurrent.Future

trait PInstanceService { this: ApiService =>

  override def createProcessInstance(itemId: String, submitter: String): Future[String] = {

    processInstance.createPInst(CreatePInstCmdRequest(
      itemID = UUID.fromString(itemId),
      by = UUID.fromString(submitter)
    )) map {
      case res: CreatePInstCmdSuccess => s"Process instance ${res.folio} created"
      case _ => "Failed"
    }
  }

  override def getProcessInstance(id: String): Future[String] = {
    val uuId = UUID.fromString(id)

    processInstance.getPInst(GetPInstCmdRequest(uuId))  map {
      case res: GetPInstCmdSuccess => s"id: ${res.id.toString}, folio: ${res.folio}, tasks : ${res.tasks.mkString(";")}"
      case _ => s"Failed to fetch ${id}"
    }
  }

  override def getPInstanceList(): Future[List[ProcessInstanceJson]] = {
    processInstance.list.map(_.map(item => ProcessInstanceJson(item.id.toString, item.folio)))
  }

  override def getPInstInitDat(): Future[PInstInitDataJson] = {

    val data = for {
      pInstList <- getPInstanceList()
      itemList <- getItemList()
      pcpList <- getParticipantList()
    } yield (pcpList, itemList, pInstList)

    data.map {f  =>
      PInstInitDataJson(f._3, f._2 , f._1)
    }
  }

  override def getPInstDetail(pInstId: UUID, taskId: UUID, accId: UUID, participantId: UUID): Future[PInstInitDataInfoJs] = {
    processInstance.getPInst(GetPInstCmdRequest(pInstId))  map {
      case res: GetPInstCmdSuccess =>
        PInstInitDataInfoJs(
          pInstId = res.id ,
          flowId = res.flowId,
          folio = res.folio,
          contentType = "",
          description = "",
          active = res.active,
          tasks = res.tasks.map{ task=>
            TaskInfoJs(
              id =  task.id,
              activity = task.activity.name,
//              actions = task.actions.map(action => ActionItemJson(action.name)),
              destinations = task.destinations.map(dest => TaskDestinationJs(dest.participantId, dest.isActive)),
              actionPerformed = task.actionPerformed.map { action =>
                ActionInfoJs(
                  id = action.id,
                  participantId = action.participantId,
                  action =  ActionItemJson(action.action.name, None),
                  actionDate = action.actionDate.toEpochMilli,
                  comment = action.comment
                )
              },
              dateCreated = task.dateCreated.toEpochMilli ,
              active=task.active
            )
          }
        )
      case _ => throw new RuntimeException("failed")
    }
  }
}
