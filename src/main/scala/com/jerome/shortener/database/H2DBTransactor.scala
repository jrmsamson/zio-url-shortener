package com.jerome.shortener.database

import cats.effect._
import com.jerome.shortener.config.AppConfig
import com.jerome.shortener.config.DbConfig
import doobie.h2.H2Transactor
import doobie.util.transactor.Transactor
import zio._
import zio.interop.catz._
import zio.interop.catz.implicits._

import scala.concurrent.ExecutionContext

case class H2DBTransactor(dbConfig: DbConfig) extends DBTransactor {

  override def getTransactor: TaskManaged[Transactor[Task]] =
    for {
      connectEC  <- ZIO.descriptor.map(_.executor.asExecutionContext).toManaged
      transactor <- mkTransactor(dbConfig, connectEC)
    } yield transactor

  private def mkTransactor(
    conf: DbConfig,
    connectEC: ExecutionContext
  ): TaskManaged[Transactor[Task]] =
    H2Transactor
      .newH2Transactor[Task](
        conf.url,
        conf.user,
        conf.password,
        connectEC
      )
      .toManagedZIO
}

object H2DBTransactor {
  val layer: RLayer[AppConfig, DBTransactor] =
    ZIO
      .service[AppConfig]
      .map(appConfig => H2DBTransactor(appConfig.dbConfig))
      .toLayer
}
