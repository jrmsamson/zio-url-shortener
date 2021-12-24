package com.jerome.shortener.repository

import com.jerome.shortener.model.GetUrlRepositoryError
import com.jerome.shortener.model.SaveUrlRepositoryError
import com.jerome.shortener.model.Url
import zio._

trait UrlRepository {
  def createTable: Task[Unit]
  def get(id: Url.Id): IO[GetUrlRepositoryError, Url]
  def save(url: String): IO[SaveUrlRepositoryError, Url]
}

object UrlRepository extends Accessible[UrlRepository] {
  def createTable: RIO[UrlRepository, Unit] =
    ZIO.serviceWithZIO(_.createTable)

  def get(id: Url.Id): ZIO[UrlRepository, GetUrlRepositoryError, Url] =
    ZIO.serviceWithZIO(_.get(id))

  def save(url: String): ZIO[UrlRepository, SaveUrlRepositoryError, Url] =
    ZIO.environmentWithZIO(_.get.save(url))
}
