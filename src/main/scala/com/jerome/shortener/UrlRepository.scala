package com.jerome.shortener

import eu.timepit.refined.api.Refined
import eu.timepit.refined.string
import zio._

trait UrlRepository {
  def createTable: Task[Unit]
  def get(id: Url.Id): IO[GetUrlRepositoryError, Url]
  def save(url: String Refined string.Url): IO[SaveUrlRepositoryError, Url]
}

object UrlRepository {
  def createTable: RIO[Has[UrlRepository], Unit]                           = ZIO.serviceWith(_.createTable)
  def get(id: Url.Id): ZIO[Has[UrlRepository], GetUrlRepositoryError, Url] = ZIO.serviceWith(_.get(id))
  def save(url: String Refined string.Url): ZIO[Has[UrlRepository], SaveUrlRepositoryError, Url] =
    ZIO.accessM(_.get.save(url))
}
