package com.jerome.shortener

import eu.timepit.refined.{refineV, string}
import io.circe.{Decoder, DecodingFailure, Encoder, HCursor, Json}
import zio.Has

package object url {
  type UrlRepository = Has[UrlRepository.Service]

  implicit val urlRequestDecoder: Decoder[UrlShortenRequest] = (c: HCursor) =>
    for {
      url      <- c.downField("url").as[String]
      validUrl <- refineV[string.Url](url).left.map(DecodingFailure(_, List.empty))
    } yield UrlShortenRequest(validUrl)

  implicit val urlRequestEncoder: Encoder[UrlShortenRequest] = (urlRequest: UrlShortenRequest) =>
    Json.obj(
      ("url", Json.fromString(urlRequest.url.value))
  )
}
