package com.dream.mytask.services

import com.dream.workflow.adaptor.dao.flow.FlowReadModelFlowImpl
import com.dream.workflow.adaptor.dao.item.ItemReadModelFlowImpl
import com.typesafe.config.ConfigFactory
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

trait ReadSideService {
  this: ApiService =>


  {
    val rootConfig = ConfigFactory.load()
    val dbConfig = DatabaseConfig.forConfig[JdbcProfile](path = "slickR", rootConfig)
    val readSideFlow = new ItemReadModelFlowImpl(dbConfig.profile, dbConfig.db)
    val flowReadModelFlow = new FlowReadModelFlowImpl(dbConfig.profile, dbConfig.db)



  }
}
