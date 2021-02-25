package com.jerome.shortener.url

import zio.test.Assertion._
import zio.test._
import zio.test.environment.TestEnvironment
import zio.test.magnolia.DeriveGen.instance
import zio.test.magnolia._

object UrlSpec extends DefaultRunnableSpec {
  override def spec: ZSpec[TestEnvironment, Any] =
    suite("Url unit tests")(
      testM("url id should be used to generate url alias") {
        implicit val genInt: DeriveGen[Int] = instance(Gen.anyInt.filter(_ > 0))
        check(DeriveGen[Url]) { url =>
          val urlShorten    = UrlShorten(url)
          val urlIdResolver = UrlIdResolver(urlShorten.shorten)
          assert(urlIdResolver.id)(equalTo(url.id))
        }
      }
    )
}
