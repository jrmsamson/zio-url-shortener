package com.jerome.shortener

import cats.effect._
import doobie.h2.H2Transactor
import doobie.util.transactor.Transactor
import zio._
import zio.interop.catz._
import zio.interop.catz.implicits._

import scala.concurrent.ExecutionContext

case class H2DBTransactor(appConfig: AppConfig) extends DBTransactor {

  override def getTransactor: TaskManaged[Transactor[Task]] =
    for {
      connectEC  <- ZIO.descriptor.map(_.executor.asExecutionContext).toManaged
      transactor <- mkTransactor(appConfig.dbConfig, connectEC)
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
  val layer: RLayer[AppConfig, DBTransactor] = (H2DBTransactor(_)).toLayer
}
