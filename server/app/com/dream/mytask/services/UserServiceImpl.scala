package com.dream.mytask.services

import java.util.UUID

import com.dream.mytask.models.{BasicInfo, User}
import com.dream.workflow.adaptor.dao.account.AccountReadModelFlowImpl
import com.dream.workflow.adaptor.dao.participant.ParticipantReadModelFlowImpl
import com.mohiva.play.silhouette.api.LoginInfo
import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class UserServiceImpl @Inject()(
  dbConfigProvider: DatabaseConfigProvider
)(implicit ex: ExecutionContext) extends UserService {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  val accountReadModelFlow = new AccountReadModelFlowImpl(dbConfig.profile, dbConfig.db)
  val participantReadModelFlows = new ParticipantReadModelFlowImpl(dbConfig.profile, dbConfig.db)

  override def retrieve(loginInfo: LoginInfo): Future[Option[User]] = {

    val userInfo = for{
      user <- accountReadModelFlow.getAccount(loginInfo.providerKey)
      participant <-  user.map(u => participantReadModelFlows.getParticipantByUser(u.id)).getOrElse(Future.successful(Seq.empty))
    } yield (user,participant)

    userInfo.map(item => item._1.map(user=> User(
      id = user.id,
      loginInfo =  loginInfo,
      userName = Some(loginInfo.providerKey),
      basicInfo = BasicInfo(Some(user.name), Some(user.fullName)),
      participants= item._2,
      roles = Set.empty
    ) ))

  }
}
