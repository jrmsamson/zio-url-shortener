package com.jerome.shortener

import zio._

trait UrlRepository {
  def createTable: Task[Unit]
  def get(id: Url.Id): IO[GetUrlRepositoryError, Url]
  def save(url: String): IO[SaveUrlRepositoryError, Url]
}

object UrlRepository {
  def createTable: RIO[UrlRepository, Unit]                           = ZIO.serviceWithZIO(_.createTable)
  def get(id: Url.Id): ZIO[UrlRepository, GetUrlRepositoryError, Url] = ZIO.serviceWithZIO(_.get(id))
  def save(url: String): ZIO[UrlRepository, SaveUrlRepositoryError, Url] =
    ZIO.environmentWithZIO(_.get.save(url))
}
