package com.jerome.shortener

import cats.effect.{ExitCode => CatsExitCode}
import com.jerome.shortener.config.AppConfig
import com.jerome.shortener.database.H2DBTransactor
import com.jerome.shortener.repository.DoobieUrlRepository
import com.jerome.shortener.repository.UrlRepository
import com.jerome.shortener.route.UrlRoutes
import doobie.util.transactor.Transactor
import org.http4s.blaze.server._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.middleware.CORS
import zio.Console._
import zio._
import zio.interop.catz._
import zio.interop.catz.implicits._
object Main extends ZIOAppDefault {

  type AppEnvironment = AppConfig with Clock with UrlRepository
  type AppTask[A]     = RIO[AppEnvironment, A]

  def run =
    (UrlRepository.createTable *> buildServer)
      .provideCustom(
        AppConfig.layer,
        H2DBTransactor.layer,
        DoobieUrlRepository.layer
      )
      .tapError(error => printLine(s"Error occurred executing service: $error"))
      .exitCode

  private def buildServer: ZIO[AppEnvironment, Throwable, Unit] =
    AppConfig.getApiConfig.flatMap(apiConfig =>
      ZIO
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
    )
}
