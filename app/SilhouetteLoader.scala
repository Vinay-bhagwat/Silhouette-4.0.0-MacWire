import com.google.inject.Provides
import com.google.inject.util.Providers
import com.mohiva.play.silhouette.api.{ Silhouette, SilhouetteProvider }
import com.mohiva.play.silhouette.crypto
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import com.mohiva.play.silhouette.impl.providers.oauth1.TwitterProvider
import com.mohiva.play.silhouette.impl.providers.oauth2.{ FacebookProvider, GoogleProvider }
import modules.{ DAOModule, SilhouetteModule, UserModule, UtilModule }
import play.api.{ Application, ApplicationLoader, BuiltInComponents, BuiltInComponentsFromContext }
import play.api.ApplicationLoader.Context
import play.api.i18n.I18nComponents
import play.api.routing.Router
import router.Routes
import com.softwaremill.macwire._
import controllers._
import models.services.{ UserService, UserServiceImpl }
import play.api.cache.EhCacheComponents
import play.api.http.HttpErrorHandler
import play.api.libs.crypto.{ CookieSigner, CookieSignerProvider, CryptoConfig, CryptoConfigParser }
import play.api.libs.mailer.MailerComponents
import play.api.libs.ws.ahc.AhcWSClient
import play.filters.csrf.CSRFComponents
import play.filters.headers.SecurityHeadersComponents
import utils.Filters
import utils.auth.DefaultEnv
//import play.api.libs.crypto.{ CookieSigner, CookieSignerProvider, CryptoConfig, CryptoConfigParser }
import play.api.libs.openid.OpenIDComponents
import play.api.libs.ws.WSClient
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.libs.ws.ning.NingWSComponents
import play.api.mvc.EssentialFilter
import utils.ErrorHandler
import com.mohiva.play.silhouette.impl.providers._
/**
 * Created by vinay.b on 1/20/2017.
 */
class SilhouetteLoader extends ApplicationLoader {
  def load(context: Context): Application = {

    (new BuiltInComponentsFromContext(context) with SilhouetteLoaderComponent).application

  }

}
trait SilhouetteLoaderComponent extends BuiltInComponents
  with I18nComponents
  with ControllerModule
  with OpenIDComponents
  with EhCacheComponents
  with DAOModule
  with MailerComponents
  with SilhouetteModule
  with CSRFComponents
  with SecurityHeadersComponents
  with UserModule
  with AhcWSComponents {
  lazy val silhouette: Silhouette[DefaultEnv] = wire[SilhouetteProvider[DefaultEnv]]
  lazy val routerOption = None
  override lazy val httpErrorHandler: HttpErrorHandler = wire[ErrorHandler]
  lazy val webJarAssets: WebJarAssets = wire[WebJarAssets]
  lazy val assets: Assets = wire[Assets]
  override lazy val router: Router = {
    lazy val prefix = "/"
    wire[Routes]
  }
  // override lazy val userService = wire[UserServiceImpl]
  override lazy val httpFilters: Seq[EssentialFilter] = {
    Seq(csrfFilter, securityHeadersFilter)
  }

  lazy val activateAccountController: ActivateAccountController = wire[ActivateAccountController]
  lazy val applicationController: ApplicationController = wire[ApplicationController]
  lazy val socialAuthController: SocialAuthController = wire[SocialAuthController]
  lazy val forgotPasswordController: ForgotPasswordController = wire[ForgotPasswordController]
  lazy val resetPasswordController: ResetPasswordController = wire[ResetPasswordController]
  lazy val changePasswordController: ChangePasswordController = wire[ChangePasswordController]
  lazy val signUpController: SignUpController = wire[SignUpController]
  lazy val signInController: SignInController = wire[SignInController]
}
