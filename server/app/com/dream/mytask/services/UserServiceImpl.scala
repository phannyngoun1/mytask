package com.dream.mytask.services

import com.dream.mytask.models.User
import com.mohiva.play.silhouette.api.LoginInfo
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

class UserServiceImpl @Inject()(
                        implicit ex: ExecutionContext//,
//                        userRepository: UserRepository,
                      ) extends UserService{

  override def retrieve(loginInfo: LoginInfo): Future[Option[User]] = {
    Future(None)
  }
}
