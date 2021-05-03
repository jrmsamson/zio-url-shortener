package com.jerome.shortener.domain.repository

import com.jerome.shortener.domain.model._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.string
import zio._

object UrlRepository {
  trait Service {
    def createTable: Task[Unit]
    def get(id: Url.Id): IO[GetUrlRepositoryError, Url]
    def save(url: String Refined string.Url): IO[SaveUrlRepositoryError, Url]
  }

  def createTable: RIO[UrlRepository, Unit]                           = ZIO.accessM(_.get.createTable)
  def get(id: Url.Id): ZIO[UrlRepository, GetUrlRepositoryError, Url] = ZIO.accessM(_.get.get(id))
  def save(url: String Refined string.Url): ZIO[UrlRepository, SaveUrlRepositoryError, Url] =
    ZIO.accessM(_.get.save(url))
}
