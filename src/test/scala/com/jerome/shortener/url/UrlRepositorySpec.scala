package com.jerome.shortener.url

import com.jerome.shortener.config.Config
import com.jerome.shortener.db.DBTransactor
import zio.Task
import zio.blocking.Blocking
import zio.test.Assertion._
import zio.test._
import zio.test.environment.TestEnvironment

object UrlRepositorySpec extends DefaultRunnableSpec {

  override def spec =
    suite("UrlRepository Integration test")(
      testM("should insert and get urls from DB") {
        for {
          _ <- UrlRepository.createTable
          nonExistingUrlId = 1234
          notFound <- UrlRepository.get(nonExistingUrlId).either
          inserted <- UrlRepository.insert("http://www.nonexistingurl.com").either
          retrieved <- inserted.fold(
            _ => Task.fail(new Throwable("Url not inserted")),
            url => UrlRepository.get(url.id).either
          )
        } yield
          assert(notFound)(isLeft(equalTo(UrlNotFound(nonExistingUrlId)))) &&
            assert(retrieved)(equalTo(inserted))
      }
    ).provideSomeLayer[TestEnvironment](
      (Config.live >+> Blocking.live >+> DBTransactor.live >+> UrlRepository.live).orDie
    )
}
