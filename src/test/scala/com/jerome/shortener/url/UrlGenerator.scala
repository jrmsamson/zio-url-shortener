package com.jerome.shortener.url

import com.jerome.shortener.domain.model.Url
import eu.timepit.refined._
import zio.random.Random
import zio.test._

object UrlGenerator {

  val genUrl: Gen[Random with Sized, Url] = for {
    id <- Gen.anyInt.map(int => if (int >= 1) int else 1)
    url <- Gen.anyString.map(
      stringUrl =>
        refineV[string.Url](s"http://www.$stringUrl.com")
          .getOrElse(refineMV[string.Url]("http://www.url.com"))
    )
  } yield Url(Url.Id(id), url)
}
