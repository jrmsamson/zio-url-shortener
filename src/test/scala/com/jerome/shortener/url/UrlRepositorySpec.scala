package com.jerome.shortener

import com.jerome.shortener.GetUrlRepositoryError._
import zio.test.Assertion._
import zio.test.ZIOSpecDefault
import zio.test.{TestConfig => _, _}

object UrlRepositorySpec extends ZIOSpecDefault {

  override def spec: ZSpec[TestEnvironment, Any] =
    suite("UrlRepository Integration test")(
      test("should insert and get urls from DB") {
        for {
          _               <- UrlRepository.createTable
          nonExistingUrlId = 1234
          notFound        <- UrlRepository.get(Url.Id(nonExistingUrlId)).either
          inserted        <- UrlRepository.save("http://www.nonexistingurl.com")
          retrieved       <- UrlRepository.get(inserted.id)
        } yield assert(notFound)(isLeft(equalTo(UrlNotFound(Url.Id(nonExistingUrlId))))) &&
          assert(retrieved)(equalTo(inserted))
      }
    ).provideCustomLayer(
      (AppConfig.layer >+> H2DBTransactor.layer >+> DoobieUrlRepository.layer).orDie
    )
}
