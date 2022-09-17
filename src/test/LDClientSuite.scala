package ld4s

import cats.effect.IO
import com.launchdarkly.sdk.server.integrations.TestData
import com.launchdarkly.sdk.{LDUser, LDValue}
import com.launchdarkly.sdk.server.LDClient as SdkClient
import munit.CatsEffectSuite

class LDClientSuite extends CatsEffectSuite {
  val json = LDValue.parse("""{"hello":"world"}""")

  val config = TestData
    .dataSource()
    .add("true", true)
    .add("1", 1)
    .add("2.5", 2.5)
    .add("hello-world", json)
    .config

  val res = ResourceFixture(LDClient.initialize[IO]("sdkKey", config))

  res.test("Retrieves values") { client =>
    val user = LDUser("u1")
    for
      _ <- client.bool("true", user, false).assertEquals(true)
      _ <- client.int("1", user, 0).assertEquals(1)
      _ <- client.double("2.5", user, 0.0).assertEquals(2.5)
      _ <- client.json("hello-world", user, LDValue.ofNull()).assertEquals(json)
    yield ()
  }

  res.test("Returns default values when flag is not found") { client =>
    val user = LDUser("u1")
    for
      _ <- client.bool("not_found", user, false).assertEquals(false)
      _ <- client.int("not_found", user, 0).assertEquals(0)
      _ <- client.double("not_found", user, 0.0).assertEquals(0.0)
      _ <- client
        .json("not_found", user, LDValue.ofNull())
        .assertEquals(LDValue.ofNull())
    yield ()
  }
}
