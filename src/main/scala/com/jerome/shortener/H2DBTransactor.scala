package com.jerome.shortener

import cats.effect.Blocker
import doobie.h2.H2Transactor
import doobie.util.transactor.Transactor
import zio._
import zio.blocking._

import scala.concurrent.ExecutionContext

case class H2DBTransactor(blocking: Blocking.Service, appConfig: AppConfig) extends DBTransactor {

  override def getTransactor: TaskManaged[Transactor[Task]] =
    for {
      connectEC  <- ZIO.descriptor.map(_.executor.asEC).toManaged_
      transactor <- mkTransactor(appConfig.dbConfig, connectEC, blocking.blockingExecutor.asEC)
    } yield transactor

  private def mkTransactor(
    conf: DbConfig,
    connectEC: ExecutionContext,
    transactEC: ExecutionContext
  ): TaskManaged[Transactor[Task]] = {
    import zio.interop.catz._
    H2Transactor
      .newH2Transactor[Task](
        conf.url,
        conf.user,
        conf.password,
        connectEC,
        Blocker.liftExecutionContext(transactEC)
      )
      .toManagedZIO
  }
}

object H2DBTransactor {
  val layer: RLayer[Has[Blocking.Service] with Has[AppConfig], Has[DBTransactor]] = (H2DBTransactor(_, _)).toLayer
}
