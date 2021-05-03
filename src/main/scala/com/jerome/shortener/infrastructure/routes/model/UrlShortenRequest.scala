package com.jerome.shortener.infrastructure.routes.model

import eu.timepit.refined.api.Refined
import eu.timepit.refined._

final case class UrlShortenRequest(url: String Refined string.Url)
