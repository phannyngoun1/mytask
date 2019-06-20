package com.dream.mytask.services

import com.dream.mytask.models.User
import com.mohiva.play.silhouette.api.services.IdentityService

trait UserService extends IdentityService[User] {

}
