package com.jerome.shortener

import com.jerome.shortener.model.GetUrlRepositoryError
import com.jerome.shortener.model.GetUrlRepositoryError._
import com.jerome.shortener.model.SaveUrlRepositoryError
import com.jerome.shortener.model.Url
import com.jerome.shortener.model.UrlId
import com.jerome.shortener.repository.UrlRepository
import zio._
import java.net.URI

class TestUrlRepository(urls: Ref[Seq[Url]]) extends UrlRepository {
  override def get(id: UrlId): IO[GetUrlRepositoryError, Url] = urls.get.flatMap { urls =>
    urls.find(_.id == id) match {
      case Some(value) => ZIO.succeed(value)
      case None        => ZIO.fail(UrlNotFound(id))
    }
  }

  override def save(url: URI): IO[SaveUrlRepositoryError, Url] =
    for {
      newId <- urls.get.map(_.size + 1)
      newUrl = Url(id = UrlId(newId), url = url)
      _     <- urls.update(urls => urls :+ newUrl)
    } yield newUrl

  override def createTable: Task[Unit] = ???
}

object TestUrlRepository {
  val test: ULayer[UrlRepository] =
    Ref.make(Seq.empty[Url]).map(new TestUrlRepository(_)).toLayer
}
