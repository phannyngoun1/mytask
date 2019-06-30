package com.dream.mytask.controllers

import java.nio.ByteBuffer
import java.util.UUID

import akka.actor.ActorSystem
import boopickle.Default._
import com.dream.mytask.services.{ApiService, UserService}
import com.dream.mytask.shared.Api
import com.dream.mytask.forms.SigninForm
import com.dream.mytask.models.User
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.util.Credentials
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import controllers.AssetsFinder
import javax.inject._
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.mvc._
import utils.auth.DefaultEnv

import scala.concurrent.{ExecutionContext, Future}

object Router extends autowire.Server[ByteBuffer, Pickler, Pickler] {

  override def read[R: Pickler](p: ByteBuffer) = Unpickle[R].fromBytes(p)

  override def write[R: Pickler](r: R) = Pickle.intoBytes(r)

}

@Singleton
class Application @Inject()(
  credentialsProvider: CredentialsProvider,
  configuration: Configuration,
  userService: UserService,
  silhouette: Silhouette[DefaultEnv],
  apiService: ApiService,
  cc: ControllerComponents

)(implicit
  assets: AssetsFinder,
  ec: ExecutionContext
) extends AbstractController(cc) with I18nSupport  {

  implicit val system: ActorSystem = ActorSystem("ticket-system")

  val id = UUID.fromString("8dbd6bf8-2f60-4e6e-8e3f-b374e060a940")

  def login = silhouette.UnsecuredAction.async { implicit request =>
    Future.successful({
      Ok(views.html.signin(SigninForm.form))
    })
  }



  def signIn = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>

    SigninForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.signin(form))),
      data => {
        val credentials = Credentials(data.userName, data.password)
        println(s"credentials  ${credentials}")
        credentialsProvider.authenticate(credentials).flatMap(loginInfo => {

          println(s"loginInfo: ${loginInfo}")

          userService.retrieve(loginInfo).flatMap {
            case Some(user) => handleActiveUser(user, loginInfo)
            case None => Future.failed(new IdentityNotFoundException("Couldn't find user"))
          }
        }).recover {
          case _ =>
            Redirect(routes.Application.signIn()).flashing("error" -> "Invalid credential")
        }
      }
    )
  }


  def singOut = silhouette.SecuredAction.async { implicit request =>

    silhouette.env.eventBus.publish(LogoutEvent(request.identity, request))
    silhouette.env.authenticatorService.discard(request.authenticator,
      Redirect(routes.Application.login())
    )
  }

  private def handleActiveUser(
    user: User,
    loginInfo: LoginInfo
  )(implicit request: RequestHeader): Future[Result] = {
    val result = Redirect(routes.Application.index())
    silhouette.env.authenticatorService.create(loginInfo)
      .flatMap { authenticator =>
        silhouette.env.eventBus.publish(LoginEvent(user, request))
        silhouette.env.authenticatorService.init(authenticator).flatMap { cookie => {
          silhouette.env.authenticatorService.embed (
            cookie,
            result
          )
        }}
      }
  }

  def index = Action {
    Ok(views.html.index(""))
  }

  def autoWireApi(path: String) = silhouette.UnsecuredAction.async(parse.raw) {
    implicit request =>

      // get the request body as ByteString
      val b = request.body.asBytes(parse.UNLIMITED).get
      //apiService.fetchUser(request.identity)
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
