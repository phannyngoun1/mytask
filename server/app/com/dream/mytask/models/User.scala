package com.dream.mytask.models

import com.mohiva.play.silhouette.api.{Identity, LoginInfo}
import com.naga.crm.shared.Common.Login

case class User(
                 id: Int,
                 loginInfo: LoginInfo,
                 userName: Option[String],
                 basicInfo: BasicInfo,
                 roles: Set[Role]
               ) extends Identity with Login


case class BasicInfo(
                    fullName : Option[String],
                    email: Option[String]
                    )