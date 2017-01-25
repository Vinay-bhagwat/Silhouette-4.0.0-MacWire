import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.actions._
import com.mohiva.play.silhouette.api.crypto.{ Crypter, CrypterAuthenticatorEncoder }
import com.mohiva.play.silhouette.api.services.AuthenticatorService
import com.mohiva.play.silhouette.api.util.{ Clock, PasswordHasherRegistry, PlayHTTPLayer }
import com.mohiva.play.silhouette.crypto.{ JcaCrypter, JcaCrypterSettings }
import com.mohiva.play.silhouette.impl.authenticators._
import com.mohiva.play.silhouette.impl.providers.{ CredentialsProvider, SocialProviderRegistry }
import com.mohiva.play.silhouette.impl.services.GravatarService
import com.mohiva.play.silhouette.impl.util.{ DefaultFingerprintGenerator, SecureRandomIDGenerator }
import com.mohiva.play.silhouette.password.BCryptPasswordHasher
import com.mohiva.play.silhouette.persistence.memory.daos.PasswordInfoDAO
import com.mohiva.play.silhouette.persistence.repositories.DelegableAuthInfoRepository
import com.softwaremill.macwire._
import models.daos.{ AuthTokenDAOImpl, UserDAOImpl }
import models.services.{ AuthTokenServiceImpl, UserServiceImpl }
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.ceedubs.ficus.readers.EnumerationReader._
import play.api.Configuration
import play.api.i18n.{ Langs, MessagesApi }
import play.api.libs.mailer.MailerClient
import play.api.libs.ws.WSClient
import utils.auth.{ CustomSecuredErrorHandler, CustomUnsecuredErrorHandler, DefaultEnv }

import scala.concurrent.ExecutionContext.Implicits.global

trait WebAppModule {

  def langs: Langs
  val config = configuration.underlying.as[JcaCrypterSettings]("silhouette.oauth1TokenSecretProvider.crypter")
  // lazy val jcaCrypterSettings = new JcaCrypterSettings("silhouette.oauth1TokenSecretProvider.crypter")
  lazy val crypter = new JcaCrypter(config)
  def messagesApi: MessagesApi

  lazy val fingerprintGenerator = new DefaultFingerprintGenerator(false)
  lazy val idGenerator = new SecureRandomIDGenerator()
  lazy val encoder = new CrypterAuthenticatorEncoder(crypter)
  def configuration: Configuration
  lazy val userDaoImpl = new UserDAOImpl
  lazy val userService = new UserServiceImpl(userDaoImpl)
  lazy val authToken = new AuthTokenDAOImpl
  lazy val authTokenService = new AuthTokenServiceImpl(authToken, clock) //(new UserDAOImpl)

  lazy val authenticatorService: AuthenticatorService[JWTAuthenticator] = {
    val config = configuration.underlying.as[JWTAuthenticatorSettings]("silhouette.jwt.authenticator")
    new JWTAuthenticatorService(config, None, encoder, idGenerator, clock)
  }
  lazy val eventBus = EventBus()

  private lazy val env: Environment[DefaultEnv] = Environment[DefaultEnv](
    userService, authenticatorService, List(), eventBus
  )

  lazy val securedErrorHandler: SecuredErrorHandler = wire[CustomSecuredErrorHandler]
  lazy val unSecuredErrorHandler: UnsecuredErrorHandler = wire[CustomUnsecuredErrorHandler]

  lazy val securedAction: SecuredAction = new DefaultSecuredAction(new DefaultSecuredRequestHandler(securedErrorHandler))
  lazy val unsecuredAction: UnsecuredAction = new DefaultUnsecuredAction(new DefaultUnsecuredRequestHandler(unSecuredErrorHandler))

  lazy val userAwareAction = new DefaultUserAwareAction(new DefaultUserAwareRequestHandler)

  lazy val passwordDao = new PasswordInfoDAO
  lazy val authInfoRepository = new DelegableAuthInfoRepository(passwordDao)

  lazy val passwordHasher = new BCryptPasswordHasher()
  lazy val passwordHasherRegistry = new PasswordHasherRegistry(passwordHasher, List())
  lazy val credentialsProvider = new CredentialsProvider(authInfoRepository, passwordHasherRegistry)

  lazy val clock = Clock()

  def wsClient: WSClient

  lazy val httpLayer = new PlayHTTPLayer(wsClient)
  lazy val avatarService = new GravatarService(httpLayer)

  lazy val silhouette: Silhouette[DefaultEnv] = wire[SilhouetteProvider[DefaultEnv]]

}
