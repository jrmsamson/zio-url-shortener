package com.jerome.shortener.model

import io.circe._
import io.circe.generic.semiauto._

import java.net.URI
import scala.util.Failure
import scala.util.Success
import scala.util.Try

final case class UrlShortenRequest(url: URI)

object UrlShortenRequest {
  implicit val encoder: Encoder[UrlShortenRequest] = deriveEncoder[UrlShortenRequest]
  implicit val decoder: Decoder[UrlShortenRequest] = deriveDecoder[UrlShortenRequest]

  implicit val uriDecoder: Decoder[URI] = Decoder.decodeString.emap { str =>
    Try(new URI(str)).toEither.left.map(throwable => s"$str is not a valid URL. Error=$throwable")
  }

  implicit val uriEncoder: Encoder[URI] = Encoder.encodeString.contramap(_.toString())
}
