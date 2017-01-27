package controllers

//import com.mohiva.play.silhouette.api.crypto.CookieSigner
import com.mohiva.play.silhouette.api.actions._
import com.mohiva.play.silhouette.api.{ Environment, Silhouette, SilhouetteProvider }
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AvatarService
import com.mohiva.play.silhouette.api.util.{ Clock, PasswordHasher, PasswordHasherRegistry }
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.providers.{ CredentialsProvider, SocialProviderRegistry }
import com.softwaremill.macwire._
import models.User
import models.daos.AuthTokenDAOImpl
import models.services.{ AuthTokenService, AuthTokenServiceImpl, UserService }
import play.api.{ Configuration, Environment }
import play.api.http.HttpErrorHandler
import play.api.i18n.MessagesApi
import play.api.routing.Router
import play.core.SourceMapper
import play.filters.csrf.{ CSRFConfig, CSRFFilter }
import play.filters.headers.SecurityHeadersFilter
import utils.Filters
import utils.auth.{ CustomSecuredErrorHandler, CustomUnsecuredErrorHandler }
//import play.api.libs.crypto.{ CookieSigner, CookieSignerProvider, CryptoConfig, CryptoConfigParser }
import play.api.libs.mailer.MailerClient
import utils.ErrorHandler
import utils.auth.DefaultEnv

trait ControllerModule {
  def messagesApi: MessagesApi
  //  def silhouetteEnvironment: Silhouette[DefaultEnv]
  def socialProviderRegistry: SocialProviderRegistry
  // def csrfHelper: CSRFHelper
  def userService: UserService
  def authInfoRepository: AuthInfoRepository
  def credentialsProvider: CredentialsProvider
  def configuration: Configuration
  def clock: Clock
  def avatarService: AvatarService
  def passwordHasher: PasswordHasher

  lazy val authToken = new AuthTokenDAOImpl
  lazy val authTokenService = new AuthTokenServiceImpl(authToken, clock)
  lazy val securedErrorHandler: SecuredErrorHandler = wire[CustomSecuredErrorHandler]
  lazy val unSecuredErrorHandler: UnsecuredErrorHandler = wire[CustomUnsecuredErrorHandler]
  lazy val userAwareAction = new DefaultUserAwareAction(new DefaultUserAwareRequestHandler)
  lazy val securedAction: SecuredAction = new DefaultSecuredAction(new DefaultSecuredRequestHandler(securedErrorHandler))
  lazy val unsecuredAction: UnsecuredAction = new DefaultUnsecuredAction(new DefaultUnsecuredRequestHandler(unSecuredErrorHandler))

}
