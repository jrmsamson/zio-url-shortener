package com.jerome.shortener

import eu.timepit.refined._
import eu.timepit.refined.api.Refined
import io.circe._
import io.circe.generic.auto._

final case class UrlShortenRequest(url: String Refined string.Url)

object UrlShortenRequest {
  implicit val stringRefinedUrlEncoder: Encoder[UrlShortenRequest] =
    Encoder.encodeString.contramap[UrlShortenRequest](_.url.value)

  implicit val stringRefinedUrlDecoder: Decoder[UrlShortenRequest] =
    Decoder.decodeString.emap(refineV[string.Url](_).map(UrlShortenRequest(_)))
}
