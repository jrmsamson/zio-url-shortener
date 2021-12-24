package com.jerome.shortener.model

import zio._
import zio.test._
import java.net.URI

object UrlGenerator {

  val genUrl: Gen[Random with Sized, Url] = for {
    id  <- Gen.int.map(int => if (int >= 1) int else 1)
    url <- Gen.alphaNumericString.map(stringUrl => s"http://www.$stringUrl.com")
  } yield Url(UrlId(id), new URI(url))
}
