package com.jerome.shortener.url

import com.jerome.shortener.infrastructure.config.Config
import com.jerome.shortener.infrastructure.config.Config._
import zio._

object TestConfig {
  val test: ULayer[Config] = ZLayer.fromFunction { _ =>
    val apiConfig = ApiConfig("localhost", 8080)
    val dbConfig  = DbConfig("", "", "", "")
    AppConfig(apiConfig, dbConfig)
  }
}
