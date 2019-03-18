package com.dream.workflow.adaptor.dao.item

trait ItemComponentSupport {
  this: ItemComponent =>

  trait ItemDaoSupport {
    this: DaoSupport[String, ItemRecord] =>

  }

}
