package com.dream.mytask.services

import com.dream.workflow.adaptor.dao.item.ItemReadModelFlowImpl
import com.dream.workflow.adaptor.journal.JournalReaderImpl
import com.dream.workflow.usecase.ReadModelUseCase
import com.typesafe.config.ConfigFactory
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

trait ReadSideService {
  this: ApiService =>
  {

    val rootConfig = ConfigFactory.load()
    val dbConfig = DatabaseConfig.forConfig[JdbcProfile](path = "slickR", rootConfig)

    print("db config")
    println(dbConfig)

    val readModelFlow = new ItemReadModelFlowImpl(dbConfig.profile, dbConfig.db)

    new ReadModelUseCase(readModelFlow, new JournalReaderImpl(system )).executeItem()

    sys.addShutdownHook {
      system.terminate()
    }

  }
}
