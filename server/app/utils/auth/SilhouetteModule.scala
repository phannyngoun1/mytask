package utils.auth

import com.dream.mytask.models.User
import com.dream.mytask.services.{SysAuthInfoRepository, UserService, UserServiceImpl}
import com.google.inject.Provides
import com.google.inject.name.Named
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.actions.{SecuredErrorHandler, UnsecuredErrorHandler}
import com.mohiva.play.silhouette.api.crypto._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services._
import com.mohiva.play.silhouette.api.util._
import com.mohiva.play.silhouette.crypto.{JcaCrypter, JcaCrypterSettings, JcaSigner, JcaSignerSettings}
import com.mohiva.play.silhouette.impl.authenticators._
import com.mohiva.play.silhouette.impl.providers._
import com.mohiva.play.silhouette.impl.util._
import com.mohiva.play.silhouette.password.{BCryptPasswordHasher, BCryptSha256PasswordHasher}
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import com.mohiva.play.silhouette.persistence.repositories.DelegableAuthInfoRepository
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.codingwell.scalaguice.ScalaModule
import play.api.Configuration
import play.api.libs.concurrent.AkkaGuiceSupport
import play.api.libs.ws.WSClient
import net.ceedubs.ficus.readers.ValueReader
import play.api.mvc.{ Cookie, CookieHeaderEncoding }
import com.typesafe.config.Config

import scala.concurrent.ExecutionContext.Implicits.global

trait DefaultEnv extends Env {
  type I = User
  type A = CookieAuthenticator
}

trait AuthEnv extends Env {
  type I = User
  type A = JWTAuthenticator
}

class SilhouetteModule extends ScalaModule with AkkaGuiceSupport {


  /**
    * A very nested optional reader, to support these cases:
    * Not set, set None, will use default ('Lax')
    * Set to null, set Some(None), will use 'No Restriction'
    * Set to a string value try to match, Some(Option(string))
    */
  implicit val sameSiteReader: ValueReader[Option[Option[Cookie.SameSite]]] =
    (config: Config, path: String) => {
      if (config.hasPathOrNull(path)) {
        if (config.getIsNull(path))
          Some(None)
        else {
          Some(Cookie.SameSite.parse(config.getString(path)))
        }
      } else {
        None
      }
    }

  override def configure(): Unit = {
    bind[UserService].to[UserServiceImpl]
    bind[EventBus].toInstance(EventBus())
    bind[UnsecuredErrorHandler].to[CustomUnsecuredErrorHandler]
    bind[SecuredErrorHandler].to[CustomSecuredErrorHandler]
    bind[IDGenerator].toInstance(new SecureRandomIDGenerator())
    bind[PasswordHasher].toInstance(new BCryptPasswordHasher)
    bind[FingerprintGenerator].toInstance(new DefaultFingerprintGenerator(false))
    bind[EventBus].toInstance(EventBus())
    bind[Clock].toInstance(Clock())
    bind[java.time.Clock].toInstance(java.time.Clock.systemUTC())
    bind[Silhouette[DefaultEnv]].to[SilhouetteProvider[DefaultEnv]]
//    bind[Silhouette[AuthEnv]].to[SilhouetteProvider[AuthEnv]]
    //bind[LdapUtil].to[LdapUtilImpl]
    bind[DelegableAuthInfoDAO[PasswordInfo]].to[SysAuthInfoRepository]
  }

  @Provides
  def provideHTTPLayer(client: WSClient): HTTPLayer = new PlayHTTPLayer(client)

  @Provides
  def provideCookieAuthenticatorEnvironment(
    userService: UserService,
    authenticatorService: AuthenticatorService[CookieAuthenticator],
    eventBus: EventBus): Environment[DefaultEnv] = {

    Environment[DefaultEnv](
      userService,
      authenticatorService,
      Seq(),
      eventBus
    )
  }

  //  @Provides
  //  def provideJWTAuthenticatorEnvironment(
  //                                          userService: UserService,
  //                                          authenticatorService: AuthenticatorService[JWTAuthenticator],
  //                                          eventBus: EventBus): Environment[AuthEnv] = {
  //
  //    Environment[AuthEnv](
  //      userService,
  //      authenticatorService,
  //      Seq(),
  //      eventBus
  //    )
  //  }

  @Provides
  def provideCookieAuthenticatorAuthenticatorService(
    @Named("authenticator-signer") signer: Signer,
    @Named("authenticator-crypter") crypter: Crypter,
    cookieHeaderEncoding: CookieHeaderEncoding,
    fingerprintGenerator: FingerprintGenerator,
    idGenerator: IDGenerator,
    configuration: Configuration,
    clock: Clock): AuthenticatorService[CookieAuthenticator] = {


    val config = configuration.underlying.as[CookieAuthenticatorSettings]("silhouette.CookieAuthenticator")
    val authenticatorEncoder = new CrypterAuthenticatorEncoder(crypter)

    new CookieAuthenticatorService(config, None, signer, cookieHeaderEncoding, authenticatorEncoder, fingerprintGenerator, idGenerator, clock)
  }

  //  @Provides
  //  def provideJWTAuthenticatorAuthenticatorService(
  //    @Named("authenticator-crypter") crypter: Crypter,
  //    idGenerator: IDGenerator,
  //    configuration: Configuration,
  //    clock: Clock): AuthenticatorService[JWTAuthenticator] = {
  //    val config = configuration.underlying.as[JWTAuthenticatorSettings]("silhouette.JwtAuthenticator")
  //    val encoder = new CrypterAuthenticatorEncoder(crypter)
  //
  //    new JWTAuthenticatorService(config, None, encoder, idGenerator, clock)
  //  }

  @Provides
  @Named("authenticator-signer")
  def provideAuthenticatorSigner(configuration: Configuration): Signer = {
    val config = configuration.underlying.as[JcaSignerSettings]("silhouette.CookieAuthenticator.signer")

    new JcaSigner(config)
  }


  @Provides
  @Named("authenticator-crypter")
  def provideAuthenticatorCrypter(configuration: Configuration): Crypter = {
    val config = configuration.underlying.as[JcaCrypterSettings]("silhouette.CookieAuthenticator.crypter")

    new JcaCrypter(config)
  }


  @Provides
  def provideAuthInfoRepository(
    passwordInfoDAO: DelegableAuthInfoDAO[PasswordInfo]): AuthInfoRepository = {

    new DelegableAuthInfoRepository(passwordInfoDAO)
  }

  @Provides
  def providePasswordHasherRegistry(): PasswordHasherRegistry = {
    PasswordHasherRegistry(new BCryptSha256PasswordHasher(), Seq(new BCryptPasswordHasher()))
  }

//    @Provides
//    def provideLdapCredentialsProvider(
//                                    authInfoRepository: AuthInfoRepository,
//                                    passwordHasherRegistry: PasswordHasherRegistry,
//                                    ldapUtil: LdapUtil
//                                  ): LdapCredentialsProvider = {
//      new LdapCredentialsProvider(authInfoRepository, passwordHasherRegistry, ldapUtil)
//    }

  @Provides
  def provideCredentialsProvider(
    authInfoRepository: AuthInfoRepository,
    passwordHasherRegistry: PasswordHasherRegistry
  ): CredentialsProvider = {
    new CredentialsProvider(authInfoRepository, passwordHasherRegistry)
  }
}
