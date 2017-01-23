package modules

import com.softwaremill.macwire._
import models.daos.UserDAO
import models.services.UserServiceImpl

trait UserModule {
  def userDAO: UserDAO

  lazy val userService = wire[UserServiceImpl]
}
