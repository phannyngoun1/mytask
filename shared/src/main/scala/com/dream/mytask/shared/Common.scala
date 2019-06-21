package com.dream.mytask.shared

object Common {

  trait Login

  case class Paging(page: Int = 1 , size:Int = 50 , total: Int = 0, hasNext: Boolean = true)

  case class SearchResult[A](
                              searchKey: String = "",
                              duration: Long = 0,
                              paging: Paging = Paging(),
                              items: Seq[A] = null
                            )

  case class Page[A](items: Seq[A], page: Int, offset: Long, total: Long) {
    lazy val prev = Option(page - 1).filter( _>=0)
    lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total )
  }

  case class ResponseMsg(id: Option[Int] = None , isSuccess: Boolean = false, message: Option[String] = None, techMessage: Option[String] = None, errors: List[(String, String,String)] = List(), loadTime: Int = -1)


  object RptFormat extends Enumeration {
    val default = Value("default")
    val excel = Value("excel")
    val pdf = Value("pdf")
  }

}
