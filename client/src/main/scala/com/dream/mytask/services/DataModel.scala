package com.dream.mytask.services

import com.dream.mytask.shared.data.TaskItem
import diode.data.Pot

object DataModel {

  case class RootModel(

    message: Pot[String],

    taskModel: TaskModel = TaskModel(),

    processInst: ProcessInstanceData = ProcessInstanceData()

  )

  case class TaskModel(
    taskList: Pot[List[TaskItem]] = Pot.empty
  )

  case class ProcessInstanceData(
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
