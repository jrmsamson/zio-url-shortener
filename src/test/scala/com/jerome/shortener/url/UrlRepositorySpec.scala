package com.jerome.shortener

import eu.timepit.refined.auto._
import zio.blocking.Blocking
import zio.test.Assertion._
import zio.test.{TestConfig => _, _}
import zio.test.environment.TestEnvironment
import com.jerome.shortener.GetUrlRepositoryError._

object UrlRepositorySpec extends DefaultRunnableSpec {

  override def spec: ZSpec[TestEnvironment, Any] =
    suite("UrlRepository Integration test")(
      testM("should insert and get urls from DB") {
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
      (AppConfig.layer >+> Blocking.live >+> H2DBTransactor.layer >+> DoobieUrlRepository.layer).orDie
    )
}
