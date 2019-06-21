package utils.auth

import com.dream.mytask.models.User
import com.mohiva.play.silhouette.api.Env
import com.mohiva.play.silhouette.impl.authenticators._


trait JWTEnv extends Env {
  type I = User
  type A = JWTAuthenticator
}