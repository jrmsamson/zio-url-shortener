package com.jerome.shortener.repository

import com.jerome.shortener.model.GetUrlRepositoryError._
import com.jerome.shortener.model.Url
import com.jerome.shortener.config.AppConfig
import com.jerome.shortener.database.H2DBTransactor
import zio.test.Assertion._
import zio.test.ZIOSpecDefault
import zio.test.{TestConfig => _, _}

object DoobieUrlRepositorySpec extends ZIOSpecDefault {

  override def spec: ZSpec[TestEnvironment, Any] =
    suite("DoobieUrlRepositorySpec")(
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
    ).provide(
      AppConfig.layer.orDie,
      H2DBTransactor.layer.orDie,
      DoobieUrlRepository.layer.orDie
    )
}
