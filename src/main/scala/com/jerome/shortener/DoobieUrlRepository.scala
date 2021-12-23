package com.jerome.shortener

import com.jerome.shortener.GetUrlRepositoryError._
import doobie._
import doobie.implicits._
import eu.timepit.refined._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.string
import zio._
import zio.interop.catz._

case class DoobieUrlRepository(dbTransactor: Transactor[Task]) extends UrlRepository {

  override def createTable: Task[Unit] =
    SQL.createTable.run
      .transact(dbTransactor)
      .foldM(error => Task.fail(error), _ => Task.succeed(()))

  override def get(id: Url.Id): IO[GetUrlRepositoryError, Url] =
    SQL
      .findById(id)
      .option
      .transact(dbTransactor)
      .foldM(
        error => IO.fail(GetUrlRepositoryError.Error(error)),
        maybeUrl => IO.require(UrlNotFound(id))(IO.succeed(maybeUrl))
      )

  override def save(url: String Refined string.Url): IO[SaveUrlRepositoryError, Url] =
    SQL
      .insertUrl(url)
      .withUniqueGeneratedKeys[Int]("id")
      .transact(dbTransactor)
      .foldM(error => IO.fail(SaveUrlRepositoryError(error)), id => IO.succeed(Url(Url.Id(id), url)))

  object SQL {
    implicit val putRefinedUrl: Put[String Refined string.Url] = Put[String].contramap(_.value)
    implicit val getRefinedUrl: Get[String Refined string.Url] = Get[String].temap(refineV[string.Url](_))

    def createTable: Update0 = sql"""
      CREATE TABLE URLS (
        id  BIGINT AUTO_INCREMENT,
        url VARCHAR NOT NULL
      )
    """.update

    def insertUrl(url: String Refined string.Url): Update0 = sql"""
      INSERT INTO URLS(url) VALUES ($url)
    """.update

    def findById(id: Url.Id): Query0[Url] = sql"""
      SELECT id, url FROM URLS WHERE id = $id
    """.query[Url]
  }
}

object DoobieUrlRepository {
  val layer: ZLayer[Has[DBTransactor], Throwable, Has[UrlRepository]] =
    DBTransactor.getTransactor.map(DoobieUrlRepository(_)).toLayer
}
