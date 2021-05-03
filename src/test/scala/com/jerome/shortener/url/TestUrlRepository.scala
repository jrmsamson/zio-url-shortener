package com.jerome.shortener.url

import com.jerome.shortener.domain.model._
import com.jerome.shortener.domain.model.GetUrlRepositoryError.UrlNotFound
import com.jerome.shortener.domain.repository.UrlRepository
import eu.timepit.refined.api.Refined
import eu.timepit.refined.string
import zio._

class TestUrlRepository(urls: Ref[Seq[Url]]) extends UrlRepository.Service {
  override def get(id: Url.Id): IO[GetUrlRepositoryError, Url] = urls.get.flatMap { urls =>
    IO.require(UrlNotFound(id))(Task.succeed(urls.find(_.id == id)))
  }
  override def save(url: String Refined string.Url): IO[SaveUrlRepositoryError, Url] = {
    for {
      newId <- urls.get.map(_.size + 1)
      newUrl = Url(id = Url.Id(newId), url = url)
      _ <- urls.update(urls => urls :+ newUrl)
    } yield newUrl
  }

  override def createTable: Task[Unit] = ???
}

object TestUrlRepository {
  val test: ULayer[UrlRepository] =
    Ref.make(Seq.empty[Url]).map(new TestUrlRepository(_)).toLayer
}
