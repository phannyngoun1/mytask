package com.dream.mytask.services

import com.dream.mytask.shared.data.AccountData.AccountItem
import com.dream.mytask.shared.data.ItemData.Item
import com.dream.mytask.shared.data.TaskItem
import diode.data.Pot

object DataModel {

  case class RootModel(

    message: Pot[String],

    taskModel: TaskModel = TaskModel(),

    processInst: ProcessInstanceModel = ProcessInstanceModel(),

    accountModel: AccountModel = AccountModel(),

    itemModel: ItemModel = ItemModel()

  )

  case class TaskModel(
    taskList: Pot[List[TaskItem]] = Pot.empty
  )

  case class AccountModel(
    accountList: Pot[List[AccountItem]] = Pot.empty
  )

  case class ItemModel(
    itemList: Pot[List[Item]]  = Pot.empty
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
