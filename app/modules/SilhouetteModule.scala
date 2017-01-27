package modules

import com.google.inject.name.Named
import com.google.inject.{ AbstractModule, Provides }
import com.mohiva.play.silhouette.api.actions.{ SecuredErrorHandler, UnsecuredErrorHandler }
import com.mohiva.play.silhouette.api.crypto._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services._
import com.mohiva.play.silhouette.api.util._
import com.mohiva.play.silhouette.api.{ Environment, EventBus, Silhouette, SilhouetteProvider }
import com.mohiva.play.silhouette.crypto.{ JcaCookieSigner, JcaCookieSignerSettings, JcaCrypter, JcaCrypterSettings }
import com.mohiva.play.silhouette.impl.authenticators._
import com.mohiva.play.silhouette.impl.providers._
import com.mohiva.play.silhouette.impl.providers.oauth1._
import com.mohiva.play.silhouette.impl.providers.oauth1.secrets.{ CookieSecretProvider, CookieSecretSettings }
import services.PlayOAuth1Service
import com.mohiva.play.silhouette.impl.providers.oauth2._
import com.mohiva.play.silhouette.impl.providers.oauth2.state.{ CookieStateProvider, CookieStateSettings, DummyStateProvider }
import com.mohiva.play.silhouette.impl.providers.openid.YahooProvider
import com.mohiva.play.silhouette.impl.providers.openid.services.PlayOpenIDService
import com.mohiva.play.silhouette.impl.services._
import com.mohiva.play.silhouette.impl.util._
import com.mohiva.play.silhouette.password.BCryptPasswordHasher
import com.mohiva.play.silhouette.persistence.daos.{ DelegableAuthInfoDAO, InMemoryAuthInfoDAO }
import com.mohiva.play.silhouette.persistence.memory.daos.{ OAuth1InfoDAO, OAuth2InfoDAO, OpenIDInfoDAO, PasswordInfoDAO }
import com.mohiva.play.silhouette.persistence.repositories.DelegableAuthInfoRepository
import models.daos._
import models.services.{ UserService, UserServiceImpl }
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.ceedubs.ficus.readers.EnumerationReader._
import net.codingwell.scalaguice.ScalaModule
import play.api.Configuration
import play.api.cache.CacheApi
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.openid.OpenIdClient
import play.api.libs.ws.WSClient
import utils.auth.{ CustomSecuredErrorHandler, CustomUnsecuredErrorHandler, DefaultEnv }
import com.softwaremill.macwire._
import models.User
import play.api.libs.concurrent.AkkaGuiceSupport
import play.libs.crypto.CookieSigner
//import play.api.libs.crypto.{ CookieSigner, CookieSignerProvider, CryptoConfig, CryptoConfigParser }
/**
 * The Guice module which wires all Silhouette dependencies.
 */

trait SilhouetteModule {
  def configuration: Configuration
  // def defaultCacheApi: CacheApi
  def wsClient: WSClient
  def openIdClient: OpenIdClient
  def userService: UserService
  def userDAO: UserDAO
  def oath1InfoDAO: OAuth1InfoDAO
  def oath2InfoDAO: OAuth2InfoDAO
  def openIDInfoDAO: OpenIDInfoDAO
  def passwordInfoDAO: PasswordInfoDAO
  // val config = configuration.underlying.as[CookieSignerSettings]("silhouette.oauth1TokenSecretProvider.cookie.signer")
  val jcaCookieSignerSettings = new JcaCookieSignerSettings("silhouette.oauth1TokenSecretProvider.crypter")
  lazy val jcacookieSigner = new JcaCookieSigner(jcaCookieSignerSettings)

  val config = configuration.underlying.as[JcaCrypterSettings]("silhouette.oauth1TokenSecretProvider.crypter")
  // lazy val jcaCrypterSettings = new JcaCrypterSettings("silhouette.oauth1TokenSecretProvider.crypter")
  lazy val crypter = new JcaCrypter(config)
  lazy val authenticatorEncoder = new Base64AuthenticatorEncoder()
  lazy val clock = Clock()
  lazy val eventBus = EventBus()
  lazy val fingerprintGenerator = new DefaultFingerprintGenerator(false)
  lazy val idGenerator = new SecureRandomIDGenerator
  lazy val passwordHasher = new BCryptPasswordHasher
  lazy val passwordHasherRegistry = new PasswordHasherRegistry(passwordHasher, List(passwordHasher))
  // lazy val cacheLayer = wire[PlayCacheLayer]
  lazy val authenticatorService = wireWith(SilhouetteAuthenticatorService.apply _)

  lazy val httpLayer = wire[PlayHTTPLayer]
  lazy val silhouetteEnvironment = wireWith(SilhouetteEnvironment.apply _)
  lazy val settings = GravatarServiceSettings()
  lazy val avatarService = wire[GravatarService]
  lazy val tokenSecretProvider = wireWith(SilhouetteOAuth1TokenSecretProvider.apply _)
  lazy val stateProvider = wire[DummyStateProvider]
  lazy val facebookProvider = wireWith(SilhouetteFacebookProvider.apply _)
  lazy val clefProvider = wireWith(SilhouetteClefProvider.apply _)
  lazy val xingProvider = wireWith(SilhouetteXingProvider.apply _)
  lazy val twitterProvider = wireWith(SilhouetteTwitterProvider.apply _)
  lazy val vKProvider = wireWith(SilhouetteVKProvider.apply _)
  lazy val googleProvider = wireWith(SilhouetteGoogleProvider.apply _)
  // lazy val yahooProvider = wireWith(SilhouetteYahooProvider.apply _)
  lazy val socialProviderRegistry = wireWith(SilhouetteSocialProviderRegistry.apply _)
  lazy val authInfoRepository = new DelegableAuthInfoRepository()
  lazy val credentialsProvider: CredentialsProvider = wireWith(SilhouetteCredentialsProvider.apply _)

  object SilhouetteAuthenticatorService {
    def apply(
      fingerprintGenerator: FingerprintGenerator,
      idGenerator: IDGenerator, authenticatorEncoder: AuthenticatorEncoder,
      clock: Clock, configuration: Configuration
    ): AuthenticatorService[JWTAuthenticator] = {
      val config = configuration.underlying.as[JWTAuthenticatorSettings]("silhouette.jwt.authenticator")
      new JWTAuthenticatorService(config, None, authenticatorEncoder, idGenerator, clock)
    }
  }

  object SilhouetteEnvironment {
    def apply(
      userService: UserService,
      authenticatorService: AuthenticatorService[JWTAuthenticator],
      eventBus: EventBus
    ): Environment[DefaultEnv] = {
      Environment[DefaultEnv](userService, authenticatorService, Seq(), eventBus)
    }
  }

  object SilhouetteOAuth1TokenSecretProvider {
    def apply(clock: Clock, cookieSigner: JcaCookieSigner, crypter: Crypter, configuration: Configuration): OAuth1TokenSecretProvider = {
      val settings = configuration.underlying.as[CookieSecretSettings]("silhouette.oauth1TokenSecretProvider")
      new CookieSecretProvider(settings, cookieSigner, crypter, clock)
    }
  }

  /* object SilhouetteOAuth2StateProvider {
    def apply(
      idGenerator: IDGenerator, clock: Clock, cookieSigner: JcaCookieSigner, configuration: Configuration
    ): OAuth2StateProvider = {
      val settings = configuration.underlying.as[CookieStateSettings]("silhouette.oauth2StateProvider")
      new CookieStateProvider(settings, idGenerator, cookieSigner, clock)
    }
  }*/

  object SilhouetteFacebookProvider {
    def apply(
      httpLayer: HTTPLayer, stateProvider: OAuth2StateProvider, configuration: Configuration
    ): FacebookProvider = {
      val settings = configuration.underlying.as[OAuth2Settings]("silhouette.facebook")
      new FacebookProvider(httpLayer, stateProvider, settings)
    }
  }

  object SilhouetteGoogleProvider {
    def apply(
      httpLayer: HTTPLayer, stateProvider: OAuth2StateProvider, configuration: Configuration
    ): GoogleProvider = {
      val settings = configuration.underlying.as[OAuth2Settings]("silhouette.google")
      new GoogleProvider(httpLayer, stateProvider, settings)
    }
  }

  object SilhouetteVKProvider {
    def apply(
      httpLayer: HTTPLayer, stateProvider: OAuth2StateProvider, configuration: Configuration
    ): VKProvider = {
      val settings = configuration.underlying.as[OAuth2Settings]("silhouette.vk")
      new VKProvider(httpLayer, stateProvider, settings)
    }
  }

  object SilhouetteTwitterProvider {
    def apply(
      httpLayer: HTTPLayer, tokenSecretProvider: OAuth1TokenSecretProvider, configuration: Configuration
    ): TwitterProvider = {
      val settings = configuration.underlying.as[OAuth1Settings]("silhouette.twitter")
      new TwitterProvider(httpLayer, new PlayOAuth1Service(settings), tokenSecretProvider, settings)
    }
  }

  object SilhouetteXingProvider {
    def apply(
      httpLayer: HTTPLayer, tokenSecretProvider: OAuth1TokenSecretProvider, configuration: Configuration
    ): XingProvider = {
      val settings = configuration.underlying.as[OAuth1Settings]("silhouette.xing")
      new XingProvider(httpLayer, new PlayOAuth1Service(settings), tokenSecretProvider, settings)
    }
  }

  /* object SilhouetteYahooProvider {
    def apply(
      cacheLayer: CacheLayer, httpLayer: HTTPLayer, client: OpenIdClient, configuration: Configuration
    ): YahooProvider = {
      val settings = configuration.underlying.as[OpenIDSettings]("silhouette.yahoo")
      new YahooProvider(httpLayer, new PlayOpenIDService(client, settings), settings)
    }
  }*/

  object SilhouetteClefProvider {
    def apply(httpLayer: HTTPLayer, configuration: Configuration): ClefProvider = {
      val settings = configuration.underlying.as[OAuth2Settings]("silhouette.clef")
      new ClefProvider(httpLayer, new DummyStateProvider, settings)
    }
  }

  object SilhouetteSocialProviderRegistry {
    def apply(
      facebookProvider: FacebookProvider,
      googleProvider: GoogleProvider,
      vkProvider: VKProvider,
      clefProvider: ClefProvider,
      twitterProvider: TwitterProvider,
      xingProvider: XingProvider /*,
      yahooProvider: YahooProvider*/
    ): SocialProviderRegistry = {
      SocialProviderRegistry(
        Seq(
          googleProvider, facebookProvider, twitterProvider,
          vkProvider, xingProvider, /* yahooProvider,*/ clefProvider
        )
      )
    }
  }

  object SilhouetteAuthInfoRepository {
    def apply(
      // passwordInfoDAO: DelegableAuthInfoDAO[],
      oauth1InfoDAO: DelegableAuthInfoDAO[OAuth1Info],
      oauth2InfoDAO: DelegableAuthInfoDAO[OAuth2Info],
      openIDInfoDAO: DelegableAuthInfoDAO[OpenIDInfo]
    ): AuthInfoRepository = {
      new DelegableAuthInfoRepository(
        oauth1InfoDAO, oauth2InfoDAO, openIDInfoDAO
      )
    }
  }

  object SilhouetteCredentialsProvider {
    def apply(
      authInfoRepository: AuthInfoRepository,
      passwordHasher: PasswordHasher,
      passwordHasherRegistry: PasswordHasherRegistry
    ): CredentialsProvider = {

      new CredentialsProvider(authInfoRepository, passwordHasherRegistry)
    }
  }
}

/*
class SilhouetteModule extends AbstractModule with ScalaModule with AkkaGuiceSupport {

  /**
    * Configures the module.
    */
  def configure() {
  bind[UserService].to[UserServiceImpl]
  bind[UserDAO].to[UserDAOImpl]
  bind[Silhouette[DefaultEnv]].to[SilhouetteProvider[DefaultEnv]]
  bind[CacheLayer].to[PlayCacheLayer]
  bind[OAuth2StateProvider].to[DummyStateProvider]
  bind[IDGenerator].toInstance(new SecureRandomIDGenerator())
  bind[PasswordHasher].toInstance(new BCryptPasswordHasher)
  bind[FingerprintGenerator].toInstance(new DefaultFingerprintGenerator(false))
  bind[EventBus].toInstance(EventBus())
  bind[Clock].toInstance(Clock())
  // Replace this with the bindings to your concrete DAOs
  bind[DelegableAuthInfoDAO[PasswordInfo]].toInstance(new InMemoryAuthInfoDAO[PasswordInfo])
  bind[DelegableAuthInfoDAO[OAuth1Info]].toInstance(new InMemoryAuthInfoDAO[OAuth1Info])
  bind[DelegableAuthInfoDAO[OAuth2Info]].toInstance(new InMemoryAuthInfoDAO[OAuth2Info])
  bind[DelegableAuthInfoDAO[OpenIDInfo]].toInstance(new InMemoryAuthInfoDAO[OpenIDInfo])

}

  /**
    * Provides the HTTP layer implementation.
    *
    * @param client Play's WS client.
    * @return The HTTP layer implementation.
    */
  @Provides
  def provideHTTPLayer(client: WSClient): HTTPLayer = new PlayHTTPLayer(client)

  /**
    * Provides the Silhouette environment.
    *
    * @param userService The user service implementation.
    * @param authenticatorService The authentication service implementation.
    * @param eventBus The event bus instance.
    * @return The Silhouette environment.
    */
  @Provides
  def provideEnvironment(
  userService: UserService,
  authenticatorService: AuthenticatorService[JWTAuthenticator],
  eventBus: EventBus): Environment[DefaultEnv] = {

  Environment[DefaultEnv](
  userService,
  authenticatorService,
  Seq(),
  eventBus
  )
}

  /**
    * Provides the social provider registry.
    *
    * @param facebookProvider The Facebook provider implementation.
    * @param googleProvider The Google provider implementation.
    * @param vkProvider The VK provider implementation.
    * @param twitterProvider The Twitter provider implementation.
    * @param xingProvider The Xing provider implementation.
    * @return The Silhouette environment.
    */
  @Provides
  def provideSocialProviderRegistry(
  facebookProvider: FacebookProvider,
  googleProvider: GoogleProvider,
  vkProvider: VKProvider,
  twitterProvider: TwitterProvider,
  xingProvider: XingProvider): SocialProviderRegistry = {

  SocialProviderRegistry(Seq(
  googleProvider,
  facebookProvider,
  twitterProvider,
  vkProvider,
  xingProvider
  ))
}

  /**
    * Provides the cookie signer for the OAuth1 token secret provider.
    *
    * @param configuration The Play configuration.
    * @return The cookie signer for the OAuth1 token secret provider.
    */
  @Provides @Named("oauth1-token-secret-cookie-signer")
  def provideOAuth1TokenSecretCookieSigner(configuration: Configuration): CookieSigner = {
  val config = configuration.underlying.as[CookiesignerSettings]("silhouette.oauth1TokenSecretProvider.cookie.signer")

  new Cookiesigner(config)
}

  /**
    * Provides the crypter for the OAuth1 token secret provider.
    *
    * @param configuration The Play configuration.
    * @return The crypter for the OAuth1 token secret provider.
    */
  @Provides @Named("oauth1-token-secret-crypter")
  def provideOAuth1TokenSecretCrypter(configuration: Configuration): Crypter = {
  val config = configuration.underlying.as[JcaCrypterSettings]("silhouette.oauth1TokenSecretProvider.crypter")

  new JcaCrypter(config)
}

  /**
    * Provides the crypter for the authenticator.
    *
    * @param configuration The Play configuration.
    * @return The crypter for the authenticator.
    */
  @Provides @Named("authenticator-crypter")
  def provideAuthenticatorCrypter(configuration: Configuration): Crypter = {
  val config = configuration.underlying.as[JcaCrypterSettings]("silhouette.authenticator.crypter")

  new JcaCrypter(config)
}

  /**
    * Provides the auth info repository.
    *
    * @param passwordInfoDAO The implementation of the delegable password auth info DAO.
    * @param oauth1InfoDAO The implementation of the delegable OAuth1 auth info DAO.
    * @param oauth2InfoDAO The implementation of the delegable OAuth2 auth info DAO.
    * @param openIDInfoDAO The implementation of the delegable OpenID auth info DAO.
    * @return The auth info repository instance.
    */
  @Provides
  def provideAuthInfoRepository(
  passwordInfoDAO: DelegableAuthInfoDAO[PasswordInfo],
  oauth1InfoDAO: DelegableAuthInfoDAO[OAuth1Info],
  oauth2InfoDAO: DelegableAuthInfoDAO[OAuth2Info],
  openIDInfoDAO: DelegableAuthInfoDAO[OpenIDInfo]): AuthInfoRepository = {

  new DelegableAuthInfoRepository(passwordInfoDAO, oauth1InfoDAO, oauth2InfoDAO, openIDInfoDAO)
}

  /**
    * Provides the authenticator service.
    *
    * @param crypter The crypter implementation.
    * @param idGenerator The ID generator implementation.
    * @param configuration The Play configuration.
    * @param clock The clock instance.
    * @return The authenticator service.
    */
  @Provides
  def provideAuthenticatorService(
  @Named("authenticator-crypter") crypter: JcaCrypter,
  idGenerator: IDGenerator,
  configuration: Configuration,
  clock: Clock): AuthenticatorService[JWTAuthenticator] = {

  val config = configuration.underlying.as[JWTAuthenticatorSettings]("silhouette.jwt.authenticator")
  val encoder = new CrypterAuthenticatorEncoder(crypter)

  new JWTAuthenticatorService(config, None, encoder, idGenerator, clock)
}

  /**
    * Provides the avatar service.
    *
    * @param httpLayer The HTTP layer implementation.
    * @return The avatar service implementation.
    */
  @Provides
  def provideAvatarService(httpLayer: HTTPLayer): AvatarService = new GravatarService(httpLayer)

  /**
    * Provides the OAuth1 token secret provider.
    *
    * @param cookieSigner The cookie signer implementation.
    * @param crypter The crypter implementation.
    * @param configuration The Play configuration.
    * @param clock The clock instance.
    * @return The OAuth1 token secret provider implementation.
    */
  @Provides
  def provideOAuth1TokenSecretProvider(
  @Named("oauth1-token-secret-cookie-signer") cookieSigner: CookieSigner,
  @Named("oauth1-token-secret-crypter") crypter: Crypter,
  configuration: Configuration,
  clock: Clock): OAuth1TokenSecretProvider = {

  val settings = configuration.underlying.as[CookieSecretSettings]("silhouette.oauth1TokenSecretProvider")
  new CookieSecretProvider(settings, cookieSigner, crypter, clock)
}

  /**
    * Provides the password hasher registry.
    *
    * @param passwordHasher The default password hasher implementation.
    * @return The password hasher registry.
    */
  @Provides
  def providePasswordHasherRegistry(passwordHasher: PasswordHasher): PasswordHasherRegistry = {
  new PasswordHasherRegistry(passwordHasher)
}

  /**
    * Provides the credentials provider.
    *
    * @param authInfoRepository The auth info repository implementation.
    * @param passwordHasherRegistry The password hasher registry.
    * @return The credentials provider.
    */
  @Provides
  def provideCredentialsProvider(
  authInfoRepository: AuthInfoRepository,
  passwordHasherRegistry: PasswordHasherRegistry): CredentialsProvider = {

  new CredentialsProvider(authInfoRepository, passwordHasherRegistry)
}

  /**
    * Provides the Facebook provider.
    *
    * @param httpLayer The HTTP layer implementation.
    * @param stateProvider The OAuth2 state provider implementation.
    * @param configuration The Play configuration.
    * @return The Facebook provider.
    */
  @Provides
  def provideFacebookProvider(
  httpLayer: HTTPLayer,
  stateProvider: OAuth2StateProvider,
  configuration: Configuration): FacebookProvider = {

  new FacebookProvider(httpLayer, stateProvider, configuration.underlying.as[OAuth2Settings]("silhouette.facebook"))
}

  /**
    * Provides the Google provider.
    *
    * @param httpLayer The HTTP layer implementation.
    * @param stateProvider The OAuth2 state provider implementation.
    * @param configuration The Play configuration.
    * @return The Google provider.
    */
  @Provides
  def provideGoogleProvider(
  httpLayer: HTTPLayer,
  stateProvider: OAuth2StateProvider,
  configuration: Configuration): GoogleProvider = {

  new GoogleProvider(httpLayer, stateProvider, configuration.underlying.as[OAuth2Settings]("silhouette.google"))
}

  /**
    * Provides the VK provider.
    *
    * @param httpLayer The HTTP layer implementation.
    * @param stateProvider The OAuth2 state provider implementation.
    * @param configuration The Play configuration.
    * @return The VK provider.
    */
  @Provides
  def provideVKProvider(
  httpLayer: HTTPLayer,
  stateProvider: OAuth2StateProvider,
  configuration: Configuration): VKProvider = {

  new VKProvider(httpLayer, stateProvider, configuration.underlying.as[OAuth2Settings]("silhouette.vk"))
}

  /**
    * Provides the Twitter provider.
    *
    * @param httpLayer The HTTP layer implementation.
    * @param tokenSecretProvider The token secret provider implementation.
    * @param configuration The Play configuration.
    * @return The Twitter provider.
    */
  @Provides
  def provideTwitterProvider(
  httpLayer: HTTPLayer,
  tokenSecretProvider: OAuth1TokenSecretProvider,
  configuration: Configuration): TwitterProvider = {

  val settings = configuration.underlying.as[OAuth1Settings]("silhouette.twitter")
  new TwitterProvider(httpLayer, new PlayOAuth1Service(settings), tokenSecretProvider, settings)
}

  /**
    * Provides the Xing provider.
    *
    * @param httpLayer The HTTP layer implementation.
    * @param tokenSecretProvider The token secret provider implementation.
    * @param configuration The Play configuration.
    * @return The Xing provider.
    */
  @Provides
  def provideXingProvider(
  httpLayer: HTTPLayer,
  tokenSecretProvider: OAuth1TokenSecretProvider,
  configuration: Configuration): XingProvider = {

  val settings = configuration.underlying.as[OAuth1Settings]("silhouette.xing")
  new XingProvider(httpLayer, new PlayOAuth1Service(settings), tokenSecretProvider, settings)
}
}*/
