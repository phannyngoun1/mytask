package com.dream.mytask.controllers

import java.nio.ByteBuffer
import java.util.UUID

import akka.actor.ActorSystem
import boopickle.Default._
import com.dream.mytask.services.ApiService
import com.dream.mytask.shared.Api
import javax.inject._
import play.api.mvc._

import scala.concurrent.ExecutionContext

object Router extends autowire.Server[ByteBuffer, Pickler, Pickler] {
  override def read[R: Pickler](p: ByteBuffer) = Unpickle[R].fromBytes(p)

  override def write[R: Pickler](r: R) = Pickle.intoBytes(r)

}

@Singleton
class Application @Inject()(
  cc: ControllerComponents,

)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  implicit val system: ActorSystem = ActorSystem("ticket-system")

  val id = UUID.fromString("8dbd6bf8-2f60-4e6e-8e3f-b374e060a940")
  val apiService = new ApiService(id)

  def index = Action {
    Ok(views.html.index(""))
  }

  def autoWireApi(path: String) = Action.async(parse.raw) {
    implicit request =>

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
