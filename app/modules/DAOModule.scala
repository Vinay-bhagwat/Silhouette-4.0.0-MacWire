package modules

import com.mohiva.play.silhouette.persistence.memory.daos.{ OAuth1InfoDAO, OAuth2InfoDAO, OpenIDInfoDAO, PasswordInfoDAO }
import com.softwaremill.macwire._
import models.daos.{ UserDAO, UserDAOImpl }

trait DAOModule {

  lazy val userDAO: UserDAO = wire[UserDAOImpl]
  lazy val oath1InfoDAO = wire[OAuth1InfoDAO]
  lazy val oath2InfoDAO = wire[OAuth2InfoDAO]
  lazy val openIDInfoDAO = wire[OpenIDInfoDAO]
  lazy val passwordInfoDAO = wire[PasswordInfoDAO]
}
