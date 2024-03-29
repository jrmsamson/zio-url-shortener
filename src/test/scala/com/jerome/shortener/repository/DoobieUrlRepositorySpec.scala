package com.jerome.shortener.repository

import com.jerome.shortener.config.AppConfig
import com.jerome.shortener.database.H2DBTransactor
import com.jerome.shortener.model.GetUrlRepositoryError._
import com.jerome.shortener.model.Url
import com.jerome.shortener.model.UrlId
import zio.test.Assertion._
import zio.test.ZIOSpecDefault
import zio.test.{TestConfig => _, _}
import java.net.URI

object DoobieUrlRepositorySpec extends ZIOSpecDefault {

  override def spec: ZSpec[TestEnvironment, Any] =
    suite("DoobieUrlRepositorySpec")(
      test("should insert and get urls from DB") {
        for {
          _               <- UrlRepository.createTable
          nonExistingUrlId = 1234
          notFound        <- UrlRepository.get(UrlId(nonExistingUrlId)).either
          inserted        <- UrlRepository.save(new URI("http://www.nonexistingurl.com"))
          retrieved       <- UrlRepository.get(inserted.id)
        } yield assert(notFound)(isLeft(equalTo(UrlNotFound(UrlId(nonExistingUrlId))))) &&
          assert(retrieved)(equalTo(inserted))
      }
    ).provide(
      AppConfig.layer.orDie,
      H2DBTransactor.layer.orDie,
      DoobieUrlRepository.layer.orDie
    )
}
