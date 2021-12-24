package com.jerome.shortener.model

import zio._
import zio.test._

object UrlGenerator {

  val genUrl: Gen[Random with Sized, Url] = for {
    id  <- Gen.int.map(int => if (int >= 1) int else 1)
    url <- Gen.string.map(stringUrl => s"http://www.$stringUrl.com")
  } yield Url(Url.Id(id), url)
}
