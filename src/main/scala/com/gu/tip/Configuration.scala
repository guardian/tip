package com.gu.tip

import com.typesafe.config.{Config, ConfigFactory}
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._

// $COVERAGE-OFF$

case class TipConfig(owner: String, repo: String, personalAccessToken: String, label: String)

object Configuration {
  val config: Config = ConfigFactory.load()
  val tip = config.as[TipConfig]("tip")
}

// $COVERAGE-ON$
