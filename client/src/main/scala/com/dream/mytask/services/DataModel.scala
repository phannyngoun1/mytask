package com.dream.mytask.services

import com.dream.mytask.shared.data.TaskItem
import diode.data.Pot

object DataModel {

  case class RootModel(

    message: Pot[String],

    taskModel: TaskModel = TaskModel(),

    processInst: ProcessInstanceModel = ProcessInstanceModel()

  )

  case class TaskModel(
    taskList: Pot[List[TaskItem]] = Pot.empty
  )

  case class AccountModel(

  )

  case class ItemModel(
    itemList: Pot[List[Item]]
  )

  case class Item(
    id: String,
    name: String
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
