package utils

import com.mohiva.play.silhouette.api.actions.SecuredErrorHandler
import org.slf4j.LoggerFactory
import play.api.Configuration
import play.api.http.DefaultHttpErrorHandler
import play.api.i18n.Messages
import play.api.mvc.Results._
import play.api.mvc.{ RequestHeader, Result }
import play.api.routing.Router
import play.core.SourceMapper

import scala.concurrent.Future

/**
 * A secured error handler.
 */
class ErrorHandler(
  env: play.api.Environment, config: Configuration,
  sourceMapper: Option[SourceMapper], router: Option[Router]
) extends DefaultHttpErrorHandler(env, config, sourceMapper, router) {

  /**
   * Called when a user is not authenticated.
   *
   * As defined by RFC 2616, the status code of the response should be 401 Unauthorized.
   *
   * //@param request The request header.
   * //@param messages The messages for the current language.
   * //@return The result to send to the client.
   */
  /* def onNotAuthenticated(request: RequestHeader, messages: Messages): Option[Future[Result]] = {
    Some(Future.successful(Redirect(controllers.routes.ApplicationController.index())))
  }

  /**
   * Called when a user is authenticated but not authorized.
   *
   * As defined by RFC 2616, the status code of the response should be 403 Forbidden.
   *
   * @param request The request header.
   * @param messages The messages for the current language.
   * @return The result to send to the client.
   */
  override def onNotAuthorized(request: RequestHeader, messages: Messages): Option[Future[Result]] = {
    Some(Future.successful(Redirect(controllers.routes.ApplicationController.index()).flashing("error" -> Messages("access.denied")(messages))))

  }
*/
  private val log = LoggerFactory.getLogger(classOf[ErrorHandler])

  override def onClientError(request: RequestHeader, statusCode: Int, message: String) = {
    Future.successful(
      Status(statusCode)("A client error occurred: " + message)
    )
  }

  override def onServerError(request: RequestHeader, exception: Throwable) = {
    log.error("Server error " + exception.getMessage + "for uri" + request.uri)
    Future.successful(

      InternalServerError("A server error occurred: " + exception.getMessage)
    )
  }

}
