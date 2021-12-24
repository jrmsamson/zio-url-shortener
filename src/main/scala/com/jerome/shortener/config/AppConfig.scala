package com.jerome.shortener.config

import pureconfig.ConfigSource
import pureconfig.generic.auto._
import zio._

final case class AppConfig(apiConfig: ApiConfig, dbConfig: DbConfig)

object AppConfig {
  val layer: TaskLayer[AppConfig] =
    Task
      .attempt(ConfigSource.default.loadOrThrow[AppConfig])
      .toLayer

  val getApiConfig: URIO[AppConfig, ApiConfig] = ZIO.service[AppConfig].map(_.apiConfig)
  val getDbConfig: URIO[AppConfig, DbConfig]   = ZIO.service[AppConfig].map(_.dbConfig)
}
