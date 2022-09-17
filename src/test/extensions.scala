package ld4s

import com.launchdarkly.sdk.LDValue
import com.launchdarkly.sdk.server.LDConfig
import com.launchdarkly.sdk.server.integrations.TestData
import com.launchdarkly.sdk.server.integrations.TestData.FlagBuilder

import scala.annotation.tailrec

type FlagValue = Boolean | String | Int | Double | LDValue

extension (td: TestData)
  @tailrec
  def add(flagKey: String, value: FlagValue): TestData =
    value match
      case v: Boolean => add(flagKey, LDValue.of(v))
      case v: String  => add(flagKey, LDValue.of(v))
      case v: Int     => add(flagKey, LDValue.of(v))
      case v: Double  => add(flagKey, LDValue.of(v))
      case v: LDValue => td.update(td.flag(flagKey).valueForAll(v))

  def config: LDConfig = new LDConfig.Builder().dataSource(td).build()
