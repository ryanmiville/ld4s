package ld4s

import cats.syntax.all.*
import cats.effect.{Resource, Sync}
import com.launchdarkly.sdk.{LDUser, LDValue}
import com.launchdarkly.sdk.server.interfaces.LDClientInterface
import com.launchdarkly.sdk.server.LDClient as SdkClient
import com.launchdarkly.sdk.server.LDConfig
import LDError.*

trait LDClient[F[_]]:
  def bool(featureKey: String, user: LDUser, default: Boolean): F[Boolean]
  def string(featureKey: String, user: LDUser, default: String): F[String]
  def int(featureKey: String, user: LDUser, default: Int): F[Int]
  def double(featureKey: String, user: LDUser, default: Double): F[Double]
  def json(featureKey: String, user: LDUser, default: LDValue): F[LDValue]

private[ld4s] class LDClientImpl[F[_]](client: LDClientInterface)(using
    F: Sync[F]
) extends LDClient[F]:
  def bool(featureKey: String, user: LDUser, default: Boolean): F[Boolean] =
    F.delay(client.boolVariation(featureKey, user, default))

  def string(featureKey: String, user: LDUser, default: String): F[String] =
    F.delay(client.stringVariation(featureKey, user, default))

  def int(featureKey: String, user: LDUser, default: Int): F[Int] =
    F.delay(client.intVariation(featureKey, user, default))

  def double(featureKey: String, user: LDUser, default: Double): F[Double] =
    F.delay(client.doubleVariation(featureKey, user, default))

  def json(featureKey: String, user: LDUser, default: LDValue): F[LDValue] =
    F.delay(client.jsonValueVariation(featureKey, user, default))

object LDClient:
  def initialize[F[_]](sdkKey: String)(using
      F: Sync[F]
  ): Resource[F, LDClient[F]] =
    initialize(sdkKey, LDConfig.DEFAULT)

  def initialize[F[_]](sdkKey: String, config: LDConfig)(using
      F: Sync[F]
  ): Resource[F, LDClient[F]] =
    val acquire = F.delay(SdkClient(sdkKey, config)).flatTap { client =>
      F.raiseWhen(!client.isInitialized)(ClientInitializationError)
    }
    Resource
      .fromAutoCloseable(acquire)
      .map(new LDClientImpl(_))
