package com.jerome.shortener.url

import zio.random.Random
import zio.test.Assertion._
import zio.test._
import zio.test.magnolia.DeriveGen.instance
import zio.test.magnolia._

object UrlSpec extends DefaultRunnableSpec {
  override def spec =
    suite("Url unit tests")(
      testM("url id should be used to generate url alias") {
        val genUrl: Gen[Random with Sized, Url] = {
          implicit val genInt: DeriveGen[Int] = instance(Gen.anyInt.filter(_ > 0))
          DeriveGen[Url]
        }

        check(genUrl) { url =>
          val urlShorten    = UrlShorten(url)
          val urlIdResolver = UrlIdResolver(urlShorten.shorten)
          assert(urlIdResolver.id)(equalTo(url.id))
        }
      }
    )
}
