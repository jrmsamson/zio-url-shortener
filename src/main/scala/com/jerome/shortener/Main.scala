package com.jerome.shortener

import cats.effect.{ExitCode => CatsExitCode}
import doobie.util.transactor.Transactor
import org.http4s.blaze.server._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.middleware.CORS
import zio.Console._
import zio.ZIOAppDefault
import zio._
import zio.interop.catz._
import zio.interop.catz.implicits._

object Main extends ZIOAppDefault {

  type AppEnvironment = AppConfig with Clock with UrlRepository
  type AppTask[A]     = RIO[AppEnvironment, A]

  private val dbTransactorLayer  = AppConfig.layer >>> H2DBTransactor.layer
  private val urlRepositoryLayer = dbTransactorLayer >>> DoobieUrlRepository.layer

  private val appLayer = urlRepositoryLayer ++ AppConfig.layer

  def run = {
    val program: ZIO[AppEnvironment, Throwable, Unit] =
      for {
        _ <- UrlRepository.createTable
        _ <- buildServer
      } yield ()

    program
      .provideCustomLayer(appLayer)
      .tapError(error => printLine(s"Error occurred executing service: $error"))
      .exitCode
  }

  private def buildServer: ZIO[AppEnvironment, Throwable, Unit] =
    for {
      apiConfig <- AppConfig.apiConfig
      _ <- ZIO
             .runtime[AppEnvironment]
             .flatMap { implicit rts =>
               val httpApp = Router[AppTask](
                 "" -> UrlRoutes.routes
               ).orNotFound

               BlazeServerBuilder[AppTask]
                 .withExecutionContext(rts.runtimeConfig.executor.asExecutionContext)
                 .bindHttp(apiConfig.port, apiConfig.baseUrl)
                 .withHttpApp(httpApp)
                 .serve
                 .compile[AppTask, AppTask, CatsExitCode]
                 .drain
             }
    } yield ()
}
