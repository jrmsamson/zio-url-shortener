package com.jerome.shortener.repository

import com.jerome.shortener.database.DBTransactor
import com.jerome.shortener.model.GetUrlRepositoryError
import com.jerome.shortener.model.GetUrlRepositoryError.UrlNotFound
import com.jerome.shortener.model.SaveUrlRepositoryError
import com.jerome.shortener.model.Url
import com.jerome.shortener.model.UrlId
import doobie._
import doobie.implicits._
import zio._
import zio.interop.catz._

import java.net.URI
import scala.util.Try

case class DoobieUrlRepository(dbTransactor: Transactor[Task]) extends UrlRepository {

  override def createTable: Task[Unit] =
    SQL.createTable.run
      .transact(dbTransactor)
      .foldZIO(error => Task.fail(error), Function.const(Task.succeed(())))

  override def get(id: UrlId): IO[GetUrlRepositoryError, Url] =
    SQL
      .findById(id)
      .option
      .transact(dbTransactor)
      .foldZIO(
        error => IO.fail(GetUrlRepositoryError.Error(error)),
        {
          case Some(url) => ZIO.succeed(url)
          case None      => ZIO.fail(UrlNotFound(id))
        }
      )

  override def save(url: URI): IO[SaveUrlRepositoryError, Url] =
    SQL
      .insertUrl(url)
      .withUniqueGeneratedKeys[Int]("id")
      .transact(dbTransactor)
      .foldZIO(
        error => IO.fail(SaveUrlRepositoryError(error)),
        id => IO.succeed(Url(UrlId(id), url))
      )

  object SQL {

    def createTable: Update0 = sql"""
      CREATE TABLE URLS (
        id  BIGINT AUTO_INCREMENT,
        url VARCHAR NOT NULL
      )
    """.update

    def insertUrl(url: URI): Update0 = sql"""
      INSERT INTO URLS(url) VALUES (${url.toString()})
    """.update

    def findById(id: UrlId): Query0[Url] = {
      import Url._
      sql""" SELECT id, url FROM URLS WHERE id = $id""".query[Url]
    }
  }
}

object DoobieUrlRepository {
  val layer: ZLayer[DBTransactor, Throwable, UrlRepository] =
    DBTransactor.getTransactor.map(DoobieUrlRepository(_)).toLayer
}
