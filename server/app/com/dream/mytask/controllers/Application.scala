package com.dream.mytask.controllers

import java.nio.ByteBuffer

import boopickle.Default._
import com.dream.mytask.shared.Api
import javax.inject._
import play.api.mvc._

import scala.concurrent.ExecutionContext

object Router extends autowire.Server[ByteBuffer, Pickler, Pickler] {
  override def read[R: Pickler](p: ByteBuffer) = Unpickle[R].fromBytes(p)

  override def write[R: Pickler](r: R) = Pickle.intoBytes(r)
}


@Singleton
class Application @Inject()(cc: ControllerComponents)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  def index = Action {
    Ok(views.html.index(""))
  }


  def autoWireApi(path: String) = Action.async(parse.raw) {
    implicit request =>

      val apiService: ApiService = new ApiService()
      // get the request body as ByteString
      val b = request.body.asBytes(parse.UNLIMITED).get

      // call Autowire route
      Router.route[Api](apiService)(
        autowire.Core.Request(path.split("/"), Unpickle[Map[String, ByteBuffer]].fromBytes(b.asByteBuffer))
      ).map(buffer => {
        val data = Array.ofDim[Byte](buffer.remaining())
        buffer.get(data)
        Ok(data)
      })
  }

}
