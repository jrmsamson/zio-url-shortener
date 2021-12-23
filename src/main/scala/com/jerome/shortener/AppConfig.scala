package com.jerome.shortener

import pureconfig.ConfigSource
import pureconfig.generic.auto._
import zio._

final case class AppConfig(apiConfig: ApiConfig, dbConfig: DbConfig)
final case class ApiConfig(baseUrl: String, port: Int)
final case class DbConfig(url: String, driver: String, user: String, password: String)

object AppConfig {
  val layer: TaskLayer[Has[AppConfig]] =
    Task
      .effect(ConfigSource.default.loadOrThrow[AppConfig])
      .toLayer

  val apiConfig: URIO[Has[AppConfig], ApiConfig] = ZIO.service[AppConfig].map(_.apiConfig)
  val dbConfig: URIO[Has[AppConfig], DbConfig]   = ZIO.service[AppConfig].map(_.dbConfig)
}
