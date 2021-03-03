package com.jerome.shortener.db

import cats.effect.Blocker
import com.jerome.shortener.config.Config
import doobie.Transactor
import doobie.h2.H2Transactor
import zio.blocking.{Blocking, blockingExecutor}
import zio.{RLayer, Task, TaskManaged, ZIO, ZLayer}

import scala.concurrent.ExecutionContext

object DBTransactor {
  val live: RLayer[Config with Blocking, DBTransactor] =
    ZLayer.fromManaged(
      for {
        config     <- Config.dbConfig.toManaged_
        connectEC  <- ZIO.descriptor.map(_.executor.asEC).toManaged_
        blockingEC <- blockingExecutor.map(_.asEC).toManaged_
        transactor <- mkTransactor(config, connectEC, blockingEC)
      } yield transactor
    )

  private def mkTransactor(
      conf: Config.DbConfig,
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
