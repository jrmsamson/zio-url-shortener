package com.jerome.shortener.config

import pureconfig.ConfigSource
import pureconfig.generic.auto._
import zio.{Has, Task, TaskLayer, URIO, ZIO, ZLayer}

object Config {
  final case class AppConfig(api: ApiConfig, db: DbConfig)
  final case class ApiConfig(baseUrl: String, port: Int)
  final case class DbConfig(url: String, driver: String, user: String, password: String)

  val apiConfig: URIO[Config, ApiConfig] = ZIO.access(_.get.api)
  val dbConfig: URIO[Config, DbConfig]   = ZIO.access(_.get.db)

  val live: TaskLayer[Config] = ZLayer
    .fromEffectMany(
      Task
        .effect(ConfigSource.default.loadOrThrow[AppConfig])
        .map(Has(_))
    )
}
