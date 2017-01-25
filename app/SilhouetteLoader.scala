import com.mohiva.play.silhouette.crypto.{ JcaCookieSigner, JcaCookieSignerSettings }
import modules.{ DAOModule, SilhouetteModule, UserModule, UtilModule }
import play.api.{ Application, ApplicationLoader, BuiltInComponents, BuiltInComponentsFromContext }
import play.api.ApplicationLoader.Context
import play.api.i18n.I18nComponents
import play.api.routing.Router
import router.Routes
import com.softwaremill.macwire._
import controllers._
import models.services.UserService
import play.api.cache.EhCacheComponents
import play.api.http.HttpErrorHandler
import play.api.libs.crypto.{ CookieSigner, CookieSignerProvider, CryptoConfig, CryptoConfigParser }
//import play.api.libs.crypto.{ CookieSigner, CookieSignerProvider, CryptoConfig, CryptoConfigParser }
import play.api.libs.openid.OpenIDComponents
import play.api.libs.ws.WSClient
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.libs.ws.ning.NingWSComponents
import play.api.mvc.EssentialFilter
import utils.ErrorHandler
/**
 * Created by vinay.b on 1/20/2017.
 */
class SilhouetteLoader extends ApplicationLoader {
  def load(context: Context): Application = {

    new SilhouetteLoaderComponent(context).application

  }

}
class SilhouetteLoaderComponent(context: Context) extends BuiltInComponentsFromContext(context)
  with I18nComponents
  with SilhouetteModule
  with OpenIDComponents
  with EhCacheComponents
  with UserModule
  with DAOModule
  with AhcWSComponents
  with ControllerModule /*  with UtilModule*/ {
  /* override lazy val cryptoConfig: CryptoConfig = new CryptoConfigParser(environment, configuration).get
  override lazy val cookieSigner: CookieSigner = new CookieSignerProvider(cryptoConfig).get*/
  lazy val routerOption = None
  override lazy val cryptoConfig: CryptoConfig = new CryptoConfigParser(environment, configuration).get

  override lazy val cookieSigner: CookieSigner = new CookieSignerProvider(cryptoConfig).get
  override lazy val httpErrorHandler: HttpErrorHandler = wire[ErrorHandler]
  override lazy val httpFilters: Seq[EssentialFilter] = filters.filters
  //  lazy val webJarAssets: WebJarAssets = wire[WebJarAssets]
  lazy val assets: Assets = wire[Assets]
  override lazy val router: Router = {
    lazy val prefix = "/"
    wire[Routes]
  }
  //    lazy val activateAccountController: ActivateAccountController = wire[ActivateAccountController]
  //    lazy val applicationController: ApplicationController = wire[ApplicationController]
  //    lazy val socialAuthController: SocialAuthController = wire[SocialAuthController]
  //    lazy val forgotPasswordController: ForgotPasswordController = wire[ForgotPasswordController]
  //    lazy val resetPasswordController: ResetPasswordController = wire[ResetPasswordController]
  //    lazy val changePasswordController: ChangePasswordController = wire[ChangePasswordController]
  //    lazy val signUpController: SignUpController = wire[SignUpController]
  //    lazy val signInController: SignInController = wire[SignInController]
}
