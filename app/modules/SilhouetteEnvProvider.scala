package modules
import com.google.inject.Provides
import com.google.inject.name.Named
import com.mohiva.play.silhouette.persistence.memory.daos
import com.mohiva.play.silhouette.api.{ Environment, EventBus, Silhouette, SilhouetteProvider }
import com.mohiva.play.silhouette.api.actions._
import com.mohiva.play.silhouette.api.crypto.{ AuthenticatorEncoder, CookieSigner, Crypter, CrypterAuthenticatorEncoder }
import com.mohiva.play.silhouette.api.services.AuthenticatorService
import com.mohiva.play.silhouette.api.util._
import com.mohiva.play.silhouette.impl.authenticators.{ CookieAuthenticator, CookieAuthenticatorService, CookieAuthenticatorSettings }
import com.mohiva.play.silhouette.impl.providers.{ CredentialsProvider, SocialProviderRegistry }
import com.mohiva.play.silhouette.impl.services.GravatarService
import com.mohiva.play.silhouette.impl.util.{ DefaultFingerprintGenerator, SecureRandomIDGenerator }
import com.mohiva.play.silhouette.password.BCryptPasswordHasher
import com.mohiva.play.silhouette.persistence.memory.daos.PasswordInfoDAO
import com.mohiva.play.silhouette.persistence.repositories.DelegableAuthInfoRepository
import com.softwaremill.macwire.wire
import models.AuthToken
import models.daos.UserDAOImpl
import models.services.{ AuthTokenService, UserServiceImpl }
import play.api.Configuration
import play.api.i18n.{ Langs, MessagesApi }
import play.api.libs.ws.WSClient
import utils.auth.{ CustomSecuredErrorHandler, CustomUnsecuredErrorHandler, DefaultEnv }
import net.ceedubs.ficus.Ficus._
import play.api.libs.mailer.MailerClient
/**
 * Created by vinay.b on 1/20/2017.
 */

/*trait SilhouetteEnvProvider {
  import play.api.libs.concurrent.Execution.Implicits._

  import com.softwaremill.macwire.MacwireMacros._
  /*
  def langs: Langs

  def messagesApi: MessagesApi
  val cookieSigner: CookieSigner

  @Provides
  def provideAuthenticatorService(
                                   @Named("authenticator-cookie-signer") cookieSigner: CookieSigner,
                                   @Named("authenticator-crypter") crypter: Crypter,
                                   fingerprintGenerator: FingerprintGenerator,
                                   idGenerator: IDGenerator,
                                   configuration: Configuration,
                                   clock: Clock): AuthenticatorService[CookieAuthenticator] = {

    val config = configuration.underlying.as[CookieAuthenticatorSettings]("silhouette.authenticator")
    val encoder = new CrypterAuthenticatorEncoder(crypter)

    new CookieAuthenticatorService(config, None, cookieSigner, encoder, fingerprintGenerator, idGenerator, clock)
  }
  lazy val fingerprintGenerator = new DefaultFingerprintGenerator(false)
  lazy val idGenerator = new SecureRandomIDGenerator()
  val encoder = new CrypterAuthenticatorEncoder()
  def configuration: Configuration


  lazy val authenticatorService: AuthenticatorService[CookieAuthenticator] = {
    val config = configuration.underlying.as[CookieAuthenticatorSettings]("silhouette.authenticator")
    new CookieAuthenticatorService(config,None,fingerprintGenerator, idGenerator, clock)
  }




  lazy val securedErrorHandler: SecuredErrorHandler = wire[CustomSecuredErrorHandler]
  lazy val unSecuredErrorHandler: UnsecuredErrorHandler = wire[CustomUnsecuredErrorHandler]

  lazy val securedAction: SecuredAction = new DefaultSecuredAction(new DefaultSecuredRequestHandler(securedErrorHandler))
  lazy val unsecuredAction: UnsecuredAction = new DefaultUnsecuredAction(new DefaultUnsecuredRequestHandler(unSecuredErrorHandler))

  lazy val userAwareAction = new DefaultUserAwareAction(new DefaultUserAwareRequestHandler)

  lazy val passwordDao = new PasswordInfoDAO
  lazy val authInfoRepository = new DelegableAuthInfoRepository(passwordDao)
  lazy val passwordHasherRegistry = new PasswordHasherRegistry(passwordHasher, List(passwordHasher))
  lazy val passwordHasher = new BCryptPasswordHasher()

  lazy val credentialsProvider = new CredentialsProvider(authInfoRepository, passwordHasherRegistry)

  lazy val socialProviderRegistry = new SocialProviderRegistry(List())
  lazy val clock = Clock

  def wsClient: WSClient

  lazy val httpLayer = new PlayHTTPLayer(wsClient)
  lazy val avatarService = new GravatarService(httpLayer)
  */
  def wsClient: WSClient
  def langs: Langs
  def mailerClient: MailerClient
  def messagesApi: MessagesApi
  lazy val passwordHasherRegistry = new PasswordHasherRegistry(passwordHasher, List(passwordHasher))
  lazy val passwordHasher = new BCryptPasswordHasher()
  lazy val httpLayer = new PlayHTTPLayer(wsClient)
  lazy val avatarService = new GravatarService(httpLayer)
  lazy val credentialsProvider = new CredentialsProvider(authInfoRepository, passwordHasherRegistry)
  lazy val securedErrorHandler: SecuredErrorHandler = wire[CustomSecuredErrorHandler]
  lazy val unSecuredErrorHandler: UnsecuredErrorHandler = wire[CustomUnsecuredErrorHandler]
  lazy val socialProviderRegistry = new SocialProviderRegistry(List())
  lazy val passwordDao = new PasswordInfoDAO
  lazy val authTokenService: AuthTokenService = wire[AuthTokenService]
  lazy val authInfoRepository = new DelegableAuthInfoRepository(passwordDao)
  lazy val securedAction: SecuredAction = new DefaultSecuredAction(new DefaultSecuredRequestHandler(securedErrorHandler))
  lazy val unsecuredAction: UnsecuredAction = new DefaultUnsecuredAction(new DefaultUnsecuredRequestHandler(unSecuredErrorHandler))
  lazy val userAwareAction = new DefaultUserAwareAction(new DefaultUserAwareRequestHandler)
  lazy val clock = Clock()
  lazy val userService = new UserServiceImpl(new UserDAOImpl)
  lazy val eventBus = EventBus()
  private lazy val env: Environment[DefaultEnv] = Environment[DefaultEnv](
    userService, AuthenticatorService[CookieAuthenticator], List(), eventBus
  )

  lazy val silhouette: Silhouette[DefaultEnv] = wire[SilhouetteProvider[DefaultEnv]]

}*/
