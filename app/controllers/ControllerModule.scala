package controllers

import com.mohiva.play.silhouette.api.actions._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AvatarService
import com.mohiva.play.silhouette.api.util.{ Clock, PasswordHasher }
import com.mohiva.play.silhouette.impl.providers.{ CredentialsProvider, SocialProviderRegistry }
import com.softwaremill.macwire._
import models.daos.AuthTokenDAOImpl
import models.services.{ AuthTokenServiceImpl, UserService }
import play.api.Configuration
import play.api.i18n.MessagesApi
import utils.auth.{ CustomSecuredErrorHandler, CustomUnsecuredErrorHandler }

trait ControllerModule {
  lazy val authToken = new AuthTokenDAOImpl
  lazy val authTokenService = new AuthTokenServiceImpl(authToken, clock)
  lazy val securedErrorHandler: SecuredErrorHandler = wire[CustomSecuredErrorHandler]
  lazy val unSecuredErrorHandler: UnsecuredErrorHandler = wire[CustomUnsecuredErrorHandler]
  lazy val userAwareAction = new DefaultUserAwareAction(new DefaultUserAwareRequestHandler)
  lazy val securedAction: SecuredAction = new DefaultSecuredAction(new DefaultSecuredRequestHandler(securedErrorHandler))
  lazy val unsecuredAction: UnsecuredAction = new DefaultUnsecuredAction(new DefaultUnsecuredRequestHandler(unSecuredErrorHandler))

  def messagesApi: MessagesApi

  def socialProviderRegistry: SocialProviderRegistry

  def userService: UserService

  def authInfoRepository: AuthInfoRepository

  def credentialsProvider: CredentialsProvider

  def configuration: Configuration

  def clock: Clock

  def avatarService: AvatarService

  def passwordHasher: PasswordHasher

}
