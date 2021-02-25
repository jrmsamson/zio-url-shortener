package com.jerome.shortener

import cats.effect.{ExitCode => CatsExitCode}
import com.jerome.shortener.config.Config
import com.jerome.shortener.db.DBTransactor
import com.jerome.shortener.url.{UrlRepository, UrlService}
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.CORS
import zio._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.console.{Console, putStrLn}
import zio.interop.catz._
import zio.logging.Logging
import zio.logging.slf4j.Slf4jLogger

object Main extends App {

  type AppEnvironment = Config with Logging with Clock with Console with DBTransactor with UrlRepository
  type AppTask[A]     = RIO[AppEnvironment, A]

  private val appLayer =
    Slf4jLogger.make((_, msg) => msg) >+>
      Config.live >+>
      Blocking.live >+>
      DBTransactor.live >+>
      UrlRepository.live

  override def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] = {
    val program: ZIO[AppEnvironment, Throwable, Unit] =
      for {
        _   <- UrlRepository.createTable
        api <- Config.apiConfig
        _ <- ZIO.runtime[AppEnvironment].flatMap { implicit rts =>
          val httpApp = Router[AppTask](
            "" -> UrlService.routes
          ).orNotFound

          BlazeServerBuilder[AppTask](rts.platform.executor.asEC)
            .bindHttp(api.port, api.baseUrl)
            .withHttpApp(CORS(httpApp))
            .serve
            .compile[AppTask, AppTask, CatsExitCode]
            .drain
        }
      } yield ()

    program
      .provideCustomLayer(appLayer)
      .tapError(error => putStrLn(s"Error occurred executing service: $error"))
      .exitCode
  }

}
