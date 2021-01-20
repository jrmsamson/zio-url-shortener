package com.jerome.shortener.url

import zio.{Ref, Task, ULayer, ZLayer}

class TestUrlRepository(urls: Ref[Seq[Url]]) extends UrlRepository.Service {
  override def get(id: Int): Task[Url] = urls.get.flatMap { urls =>
    Task.require(UrlNotFound(id))(Task.succeed(urls.find(_.id == id)))
  }
  override def insert(url: String): Task[Url] = {
    for {
      newId <- urls.get.map(_.size + 1)
      newUrl = Url(id = newId, url = url)
      _ <- urls.update(urls => urls :+ newUrl)
    } yield newUrl
  }

  override def createTable: Task[Unit] = ???
}

object TestUrlRepository {
  val test: ULayer[UrlRepository] =
    ZLayer.fromEffect(Ref.make(Seq.empty[Url]).map(new TestUrlRepository(_)))
}
