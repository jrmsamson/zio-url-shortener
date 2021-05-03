package com.jerome.shortener.url

import com.jerome.shortener.domain.model.Url
import zio.test.Assertion._
import zio.test._
import zio.test.environment.TestEnvironment
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.refineV

object UrlSpec extends DefaultRunnableSpec {
  override def spec: ZSpec[TestEnvironment, Any] =
    suite("Url unit tests")(
      testM("url id should be used to generate url alias") {
        check(UrlGenerator.genUrl) { url =>
          refineV[NonEmpty](url.shorten).fold(
            _ => assertCompletes,
            urlShorten => assert(Url.idFromAlias(urlShorten))(equalTo(url.id))
          )
        }
      }
    )
}
