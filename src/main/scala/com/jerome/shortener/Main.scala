package com.jerome.shortener

import cats.effect.{ExitCode => CatsExitCode}
import doobie.util.transactor.Transactor
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.CORS
import zio._
import zio.blocking._
import zio.clock._
import zio.console._
import zio.interop.catz._
import zio.logging._
import zio.logging.slf4j.Slf4jLogger

object Main extends App {

  type AppEnvironment = Has[AppConfig] with Logging with Clock with Has[UrlRepository]
  type AppTask[A]     = RIO[AppEnvironment, A]

  private val dbTransactorLayer  = (AppConfig.layer ++ Blocking.live) >>> H2DBTransactor.layer
  private val urlRepositoryLayer = dbTransactorLayer >>> DoobieUrlRepository.layer

  private val appLayer =
    Slf4jLogger.make((_, msg) => msg) ++
      urlRepositoryLayer ++
      AppConfig.layer

  override def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] = {
    val program: ZIO[AppEnvironment, Throwable, Unit] =
      for {
        _ <- UrlRepository.createTable
        _ <- buildServer
      } yield ()

    program
      .provideCustomLayer(appLayer)
      .tapError(error => putStrLn(s"Error occurred executing service: $error"))
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

               BlazeServerBuilder[AppTask](rts.platform.executor.asEC)
                 .bindHttp(apiConfig.port, apiConfig.baseUrl)
                 .withHttpApp(CORS(httpApp))
                 .serve
                 .compile[AppTask, AppTask, CatsExitCode]
                 .drain
             }
    } yield ()
}
