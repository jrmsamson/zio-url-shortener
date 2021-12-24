package com.jerome.shortener.repository

import com.jerome.shortener.model.GetUrlRepositoryError
import com.jerome.shortener.model.SaveUrlRepositoryError
import com.jerome.shortener.model.Url
import com.jerome.shortener.model.UrlId
import zio._

trait UrlRepository {
  def createTable: Task[Unit]
  def get(id: UrlId): IO[GetUrlRepositoryError, Url]
  def save(url: String): IO[SaveUrlRepositoryError, Url]
}

object UrlRepository extends Accessible[UrlRepository] {
  def createTable: RIO[UrlRepository, Unit] =
    ZIO.serviceWithZIO(_.createTable)

  def get(id: UrlId): ZIO[UrlRepository, GetUrlRepositoryError, Url] =
    ZIO.serviceWithZIO(_.get(id))

  def save(url: String): ZIO[UrlRepository, SaveUrlRepositoryError, Url] =
    ZIO.environmentWithZIO(_.get.save(url))
}
