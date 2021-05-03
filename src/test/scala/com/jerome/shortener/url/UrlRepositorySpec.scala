package com.jerome.shortener.url

import com.jerome.shortener.infrastructure.config.Config
import com.jerome.shortener.infrastructure.database.H2DBTransactor
import com.jerome.shortener.domain.model.GetUrlRepositoryError.UrlNotFound
import com.jerome.shortener.domain.model.Url
import com.jerome.shortener.domain.repository.UrlRepository
import com.jerome.shortener.infrastructure.repository.DoobieUrlRepository
import eu.timepit.refined.auto._
import zio.blocking.Blocking
import zio.test.Assertion._
import zio.test._
import zio.test.environment.TestEnvironment

object UrlRepositorySpec extends DefaultRunnableSpec {

  override def spec: ZSpec[TestEnvironment, Any] =
    suite("UrlRepository Integration test")(
      testM("should insert and get urls from DB") {
        for {
          _ <- UrlRepository.createTable
          nonExistingUrlId = 1234
          notFound  <- UrlRepository.get(Url.Id(nonExistingUrlId)).either
          inserted  <- UrlRepository.save("http://www.nonexistingurl.com")
          retrieved <- UrlRepository.get(inserted.id)
        } yield
          assert(notFound)(isLeft(equalTo(UrlNotFound(Url.Id(nonExistingUrlId))))) &&
            assert(retrieved)(equalTo(inserted))
      }
    ).provideCustomLayer(
      (Config.live >+> Blocking.live >+> H2DBTransactor.live >+> DoobieUrlRepository.live).orDie
    )
}
