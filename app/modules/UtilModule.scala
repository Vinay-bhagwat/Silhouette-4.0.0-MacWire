package modules

import com.mohiva.play.silhouette.api.Silhouette
import com.softwaremill.macwire._
import play.api.http.HttpErrorHandler
import play.api.routing.Router
import play.api.{ Configuration, Environment }
import play.core.SourceMapper
import play.filters.csrf.{ CSRFConfig, CSRFFilter }
import play.filters.headers.SecurityHeadersFilter
import utils.auth.DefaultEnv
import utils.{ ErrorHandler, Filters }

trait UtilModule {
  def environment: Environment
  def configuration: Configuration
  def sourceMapper: Option[SourceMapper]
  def routerOption: Option[Router]
  def csrfFilter: CSRFFilter
  def csrfConfig: CSRFConfig
  def securityHeadersFilter: SecurityHeadersFilter

  lazy val filters = wire[Filters]

  lazy val errorHandler: HttpErrorHandler = wire[ErrorHandler]
}
