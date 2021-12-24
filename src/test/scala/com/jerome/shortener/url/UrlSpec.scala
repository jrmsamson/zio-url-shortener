package com.jerome.shortener

import zio.test.Assertion._
import zio.test.ZIOSpecDefault
import zio.test._

object UrlSpec extends ZIOSpecDefault {
  override def spec: ZSpec[TestEnvironment, Any] =
    suite("Url unit tests")(
      test("url id should be used to generate url alias") {
        check(UrlGenerator.genUrl) { url =>
          assert(Url.idFromAlias(url.shorten))(equalTo(url.id))
        }
      }
    )
}
