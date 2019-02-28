package com.dream.mytask.services

import com.dream.mytask.shared.data.Task
import diode.data.Pot

object DataModel {
  case class RootModel(
    message: Pot[String],
    taskModel: TaskModel = TaskModel()

  )

  case class TaskModel(
    taskList: Pot[List[Task]] = Pot.empty
  )
}
