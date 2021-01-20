package com.jerome.shortener

import com.jerome.shortener.config.Config.AppConfig
import zio._

package object config {
  type Config = Has[AppConfig]
}
