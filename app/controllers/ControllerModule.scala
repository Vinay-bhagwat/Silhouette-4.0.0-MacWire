package controllers

//import com.mohiva.play.silhouette.api.crypto.CookieSigner
import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AvatarService
import com.mohiva.play.silhouette.api.util.{ Clock, PasswordHasher, PasswordHasherRegistry }
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.providers.{ CredentialsProvider, SocialProviderRegistry }
import com.softwaremill.macwire._
import models.User
import models.services.{ AuthTokenService, UserService }
import play.api.{ Configuration, Environment }
import play.api.http.HttpErrorHandler
import play.api.i18n.MessagesApi
import play.api.routing.Router
import play.core.SourceMapper
import play.filters.csrf.{ CSRFConfig, CSRFFilter }
import play.filters.headers.SecurityHeadersFilter
import utils.Filters
//import play.api.libs.crypto.{ CookieSigner, CookieSignerProvider, CryptoConfig, CryptoConfigParser }
import play.api.libs.mailer.MailerClient
import utils.ErrorHandler
import utils.auth.DefaultEnv

trait ControllerModule {
  //  def cookieSigner: CookieSigner
  def messagesApi: MessagesApi
  def environment: Environment
  def silhouette: Silhouette[DefaultEnv]
  def socialProviderRegistry: SocialProviderRegistry
  def authTokenService: AuthTokenService
  def userService: UserService
  def authInfoRepository: AuthInfoRepository
  def credentialsProvider: CredentialsProvider
  def configuration: Configuration
  def clock: Clock
  def mailerClient: MailerClient
  def avatarService: AvatarService
  def passwordHasher: PasswordHasher
  def passwordHasherRegistry: PasswordHasherRegistry
  def sourceMapper: Option[SourceMapper]
  //  def environment: Environment
  //  def configuration: Configuration
  //  def sourceMapper: Option[SourceMapper]
  def routerOption: Option[Router]
  def csrfFilter: CSRFFilter
  def csrfConfig: CSRFConfig
  def securityHeadersFilter: SecurityHeadersFilter

  lazy val filters = wire[Filters]

  lazy val httpErrorHandler: HttpErrorHandler = wire[ErrorHandler]
  lazy val webJarAssets: WebJarAssets = wire[WebJarAssets]
  lazy val applicationController: ApplicationController = wire[ApplicationController]
  lazy val activateAccountController: ActivateAccountController = wire[ActivateAccountController]
  lazy val changePasswordController: ChangePasswordController = wire[ChangePasswordController]
  lazy val forgotPasswordController: ForgotPasswordController = wire[ForgotPasswordController]
  lazy val resetPasswordController: ResetPasswordController = wire[ResetPasswordController]
  lazy val signInController: SignInController = wire[SignInController]
  lazy val signUpController: SignUpController = wire[SignUpController]
  lazy val socialAuthController: SocialAuthController = wire[SocialAuthController]
  //lazy val webJarAssets: WebJarAssets = wire[WebJarAssets]
  // lazy val httpErrorHandler: HttpErrorHandler = wire[ErrorHandler]
}
