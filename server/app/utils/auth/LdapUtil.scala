package utils.auth

import java.security.MessageDigest

import com.unboundid.ldap.sdk._
import javax.inject.{Inject, Singleton}
import play.cache._
import utils.auth.LdapConf._

trait LdapUtil {
  def authenticate(uid: String, pass: String): ResultCode
}

@Singleton
class LdapUtilImpl @Inject()(cache: SyncCacheApi) extends LdapUtil {
  val ldapConnection = new LDAPConnection()

  initConnection()

  def initConnection() = {
    val options = new LDAPConnectionOptions()
    options.setAbandonOnTimeout(true)
    options.setConnectTimeoutMillis(5000)
    options.setAutoReconnect(true)
    options.setUseTCPNoDelay(true)
    ldapConnection.setConnectionOptions(options)
    ldapConnection.connect(ldapHost, ldapPort)
  }

  def authenticate(uid: String, pass: String): ResultCode = {
    val msg: String = uid + pass + "ela_salt_201406"
    val hash: String = MessageDigest.getInstance("SHA-256")
      .digest(msg.getBytes)
      .foldLeft("")(
        (s: String, b: Byte) =>
          s + Character.forDigit((b & 0xf0) >> 4, 16) + Character.forDigit(b & 0x0f, 16)
      )
    val cacheKey = "bindResult." + hash
    val bindResult = try {
      cache.getOrElseUpdate[ResultCode](cacheKey, () => ldapConnection.bind(uid, pass).getResultCode, ldapCacheDuration)
    } catch {
      case _: Throwable => ResultCode.CONNECT_ERROR
    }

    bindResult
  }
}
