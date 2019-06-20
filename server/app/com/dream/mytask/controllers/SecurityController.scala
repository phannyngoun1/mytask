package com.dream.mytask.controllers

import com.dream.mytask.models.User
import com.dream.mytask.services.UserService
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

@Singleton
class SecurityController @Inject()(
//                                    credentialsProvider: LdapCredentialsProvider,
                                    credentialsProvider: CredentialsProvider,
                                    configuration: Configuration,
                                    userService: UserService,
                                    silhouette: Silhouette[DefaultEnv],
                                    cc: ControllerComponents
                                  )(implicit
                                    assets: AssetsFinder,
                                    ec: ExecutionContext)
  extends AbstractController(cc) with I18nSupport {

  def index = silhouette.UnsecuredAction.async { implicit request =>
    Future.successful({
      Ok(views.html.signIn(SignInForm.form))
    })
  }


  def signIn = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>

    SignInForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.signIn(form))),
      data => {
        val credentials = Credentials(data.userName, data.password)
        credentialsProvider.authenticate(credentials).flatMap(loginInfo =>
          userService.retrieve(loginInfo).flatMap {
            case Some(user) => handleActiveUser(user, loginInfo)
            case None => Future.failed(new IdentityNotFoundException("Couldn't find user"))
          }
        ).recover {
          case _ =>
            Redirect(routes.SecurityController.signIn()).flashing("error" -> "Invalid credential")
        }
      }
    )
  }


  def singOut = silhouette.SecuredAction.async { implicit request =>

    silhouette.env.eventBus.publish(LogoutEvent(request.identity, request))
    silhouette.env.authenticatorService.discard(request.authenticator,
      Redirect(routes.SecurityController.index())
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
}
