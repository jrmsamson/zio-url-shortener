package com.jerome.shortener.url

import com.jerome.shortener.config.Config
import com.jerome.shortener.config.Config.{ApiConfig, AppConfig, DbConfig}
import zio.{ULayer, ZLayer}

object TestConfig {
  val test: ULayer[Config] = ZLayer.fromFunction { _ =>
    val apiConfig = ApiConfig("localhost", 8080)
    val dbConfig  = DbConfig("", "", "", "")
    AppConfig(apiConfig, dbConfig)
  }
}
