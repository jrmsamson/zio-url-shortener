package com.jerome.shortener

import com.jerome.shortener.GetUrlRepositoryError._
import zio._

class TestUrlRepository(urls: Ref[Seq[Url]]) extends UrlRepository {
  override def get(id: Url.Id): IO[GetUrlRepositoryError, Url] = urls.get.flatMap { urls =>
    urls.find(_.id == id) match {
      case Some(value) => ZIO.succeed(value)
      case None        => ZIO.fail(UrlNotFound(id))
    }
  }

  override def save(url: String): IO[SaveUrlRepositoryError, Url] =
    for {
      newId <- urls.get.map(_.size + 1)
      newUrl = Url(id = Url.Id(newId), url = url)
      _     <- urls.update(urls => urls :+ newUrl)
    } yield newUrl

  override def createTable: Task[Unit] = ???
}

object TestUrlRepository {
  val test: ULayer[UrlRepository] =
    Ref.make(Seq.empty[Url]).map(new TestUrlRepository(_)).toLayer
}
