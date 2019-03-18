package com.dream.mytask.services

import com.dream.mytask.shared.data.AccountData.{AccountJson, ParticipantJson}
import com.dream.mytask.shared.data.ItemData.ItemJson
import com.dream.mytask.shared.data.TaskItem
import com.dream.mytask.shared.data.WorkflowData.FlowJson
import diode.data.Pot

object DataModel {

  case class RootModel(

    message: Pot[String],

    taskModel: TaskModel = TaskModel(),

    flowModel: FlowModel = FlowModel(),

    processInst: ProcessInstanceModel = ProcessInstanceModel(),

    accountModel: AccountModel = AccountModel(),

    itemModel: ItemModel = ItemModel()

  )


  case class FlowModel(
    flowList: Pot[List[FlowJson]] = Pot.empty,
    flow: Pot[FlowJson] = Pot.empty,
    message: Pot[String] = Pot.empty
  )

  case class TaskModel(
    taskList: Pot[List[TaskItem]] = Pot.empty
  )

  case class AccountModel(
    accountList: Pot[List[AccountJson]] = Pot.empty,
    participantList: Pot[List[ParticipantJson]] = Pot.empty,
    account: Pot[AccountJson] = Pot.empty,
    participant: Pot[AccountJson] = Pot.empty
  )

  case class ItemModel(
    itemList: Pot[List[ItemJson]]  = Pot.empty,
    item: Pot[ItemJson] = Pot.empty,
    message: Pot[String] = Pot.empty
  )

  case class ProcessInstanceModel(
    criteria: Option[ProcessInstanceCriteria] = None,
    data: Pot[ProcessInstanceResult] = Pot.empty
  )

  case class ProcessInstanceCriteria(
    id: String
  )

  case class ProcessInstanceResult(
    value: String
  )

}
