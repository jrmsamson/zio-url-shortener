package com.jerome.shortener.url

import com.jerome.shortener.db.DBTransactor
import zio._
import doobie.implicits._
import doobie.{Query0, Update0}
import zio.interop.catz._

object UrlRepository {
  trait Service {
    def createTable: Task[Unit]
    def get(id: Int): Task[Url]
    def insert(url: String): Task[Url]
  }

  val live: RLayer[DBTransactor, UrlRepository] = ZLayer.fromService { dbTransactor =>
    new Service {
      override def createTable: Task[Unit] =
        SQL.createTable.run
          .transact(dbTransactor)
          .foldM(error => Task.fail(error), _ => Task.succeed(()))

      override def get(id: Int): Task[Url] =
        SQL
          .findById(id)
          .option
          .transact(dbTransactor)
          .foldM(
            error => Task.fail(error),
            maybeUrl => Task.require(UrlNotFound(id))(Task.succeed(maybeUrl))
          )

      override def insert(url: String): Task[Url] =
        SQL
          .insertUrl(url)
          .withUniqueGeneratedKeys[Int]("id")
          .transact(dbTransactor)
          .foldM(error => Task.fail(error), id => Task.succeed(Url(id, url)))
    }
  }

  object SQL {
    def createTable: Update0 = sql"""
      CREATE TABLE URLS (
        id  BIGINT AUTO_INCREMENT,
        url VARCHAR NOT NULL
      )
    """.update

    def insertUrl(url: String): Update0 = sql"""
      INSERT INTO URLS(url) VALUES ($url)
    """.update

    def findById(id: Int): Query0[Url] = sql"""
      SELECT * FROM URLS WHERE id = $id
    """.query[Url]
  }

  def createTable: RIO[UrlRepository, Unit]        = ZIO.accessM(_.get.createTable)
  def get(id: Int): RIO[UrlRepository, Url]        = ZIO.accessM(_.get.get(id))
  def insert(url: String): RIO[UrlRepository, Url] = ZIO.accessM(_.get.insert(url))
}
