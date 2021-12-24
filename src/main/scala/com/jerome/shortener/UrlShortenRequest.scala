package com.jerome.shortener

import io.circe._
import io.circe.generic.auto._

final case class UrlShortenRequest(url: String)

object UrlShortenRequest {}
