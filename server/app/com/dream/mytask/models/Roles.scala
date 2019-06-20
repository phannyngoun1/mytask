package com.dream.mytask.models

import com.mohiva.play.silhouette.api.{Authenticator, Authorization}
import com.mohiva.play.silhouette.impl.authenticators.{CookieAuthenticator, JWTAuthenticator}
import play.api.mvc.Request

import scala.concurrent.Future


/**
  * Check for authorization
  */

case class WithRole(role: Role) extends Authorization[User, CookieAuthenticator] {
  override def isAuthorized[B](user: User, authenticator: CookieAuthenticator)(
    implicit
    request: Request[B]): Future[Boolean] =   user.roles match{
    case list: Set[Role] => Future.successful(list.contains(role))
    case _               => Future.successful(false)
  }
}


trait Role {
  def name: String
}

object Role {
  def apply(role: String): Role = role match {
    case Admin.name => Admin
    case AddPlayer.name => AddPlayer
    case EditPlayer.name => EditPlayer
    case AddTrip.name => AddTrip
    case EditTrip.name => EditTrip
  }

  def unapply(role: Role): Option[String] =  Some(role.name)
}

object Admin extends Role {
  val name ="admin"
}

object AddPlayer extends Role {
  val name = "add-player"
}

object EditPlayer extends Role {
  val name = "edit-player"
}

object AddTrip extends Role {
  val name = "add-trip"
}

object EditTrip extends Role {
  val name = "edit-trip"
}

object Unknown extends Role {
  val name = "-"
}


