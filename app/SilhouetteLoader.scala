import com.mohiva.play.silhouette.api.{ Silhouette, SilhouetteProvider }
import com.softwaremill.macwire._
import controllers._
import modules.{ SilhouetteModule, UserModule }
import play.api.ApplicationLoader.Context
import play.api.cache.EhCacheComponents
import play.api.http.HttpErrorHandler
import play.api.i18n.I18nComponents
import play.api.libs.mailer.MailerComponents
import play.api.libs.openid.OpenIDComponents
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.mvc.EssentialFilter
import play.api.routing.Router
import play.api.{ Application, ApplicationLoader, BuiltInComponentsFromContext }
import play.filters.csrf.CSRFComponents
import play.filters.headers.SecurityHeadersComponents
import router.Routes
import utils.ErrorHandler
import utils.auth.DefaultEnv

class SilhouetteLoaderComponent(context: Context) extends BuiltInComponentsFromContext(context)
  with I18nComponents
  with ControllerModule
  with OpenIDComponents
  with EhCacheComponents
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

/**
 * Created by vinay.b on 1/20/2017.
 */
class SilhouetteLoader extends ApplicationLoader {
  def load(context: Context): Application = {

    (new SilhouetteLoaderComponent(context)).application

  }

}
