package com.dream.mytask.services

import com.dream.mytask.shared.data.AccountData.{AccountJson, ParticipantJson}
import com.dream.mytask.shared.data.ItemData.{ItemInitDataJs, ItemJson}
import com.dream.mytask.shared.data.ProcessInstanceData.{PInstInitDataJson, ProcessInstanceJson}
import com.dream.mytask.shared.data.{ActionInfoJson, TaskItemJson}
import com.dream.mytask.shared.data.WorkflowData.{FlowInitDataJs, FlowJson}
import diode.data.Pot

object DataModel {

  case class RootModel(

    message: Pot[String],

    taskModel: TaskModel = TaskModel(),

    flowModel: FlowModel = FlowModel(),

    processInst: ProcessInstanceModel = ProcessInstanceModel(),

    accountModel: AccountModel = AccountModel(),

    itemModel: ItemModel = ItemModel(),

    formModel: FormModel = FormModel()

  )

  case class FormModel(

    message: Pot[String] = Pot.empty,

    ticketModel: TicketModel = TicketModel(),

    actionInfo: Option[ActionInfoJson] = None

  )


  case class ActionJson()

  case class FlowModel(
    flowList: Pot[List[FlowJson]] = Pot.empty,
    flow: Pot[FlowJson] = Pot.empty,
    message: Pot[String] = Pot.empty,
    initData: Pot[FlowInitDataJs] = Pot.empty

  )

  case class TaskModel(
    taskList: Pot[List[TaskItemJson]] = Pot.empty,
    message: Pot[String] = Pot.empty
  )

  case class AccountModel(
    accountList: Pot[List[AccountJson]] = Pot.empty,
    participantList: Pot[List[ParticipantJson]] = Pot.empty,
    account: Pot[AccountJson] = Pot.empty,
    participant: Pot[ParticipantJson] = Pot.empty,
    message: Pot[String] = Pot.empty
  )

  case class ItemModel(
    itemList: Pot[List[ItemJson]]  = Pot.empty,
    item: Pot[ItemJson] = Pot.empty,
    message: Pot[String] = Pot.empty,
    initData: Pot[ItemInitDataJs] = Pot.empty
  )

  case class TicketModel()

  case class ProcessInstanceModel(
    criteria: Option[ProcessInstanceCriteria] = None,
    list: Pot[List[ProcessInstanceJson]] = Pot.empty,
    item: Pot[ProcessInstanceJson] = Pot.empty,
    data: Pot[ProcessInstanceResult] = Pot.empty,
    initData: Pot[PInstInitDataJson] = Pot.empty
  )

  case class ProcessInstanceCriteria(
    id: String
  )

  case class ProcessInstanceResult(
    value: String
  )

}
