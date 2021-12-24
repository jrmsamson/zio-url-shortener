package com.jerome.shortener

import zio._

object TestConfig {
  val test: ULayer[AppConfig] = ZLayer.fromFunction { _ =>
    val apiConfig = ApiConfig("localhost", 8080)
    val dbConfig  = DbConfig("", "", "", "")
    AppConfig(apiConfig, dbConfig)
  }
}
