package utils.auth

import com.typesafe.config.ConfigFactory

object LdapConf {
  val conf = ConfigFactory.load()
  val ldapHost = conf.getString("ldap.ldapHost")
  val ldapPort = conf.getInt("ldap.ldapPort")
  val ldapCacheDuration = conf.getInt("ldap.ldapCacheDuration")
}
