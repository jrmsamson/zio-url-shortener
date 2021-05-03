package com.jerome.shortener.url

import com.jerome.shortener.domain.repository.UrlRepository
import com.jerome.shortener.infrastructure.config.Config
import com.jerome.shortener.infrastructure.routes.model._
import io.circe._
import org.http4s._
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.headers.Location
import org.http4s.implicits._
import zio.RIO
import zio.interop.catz._
import zio.logging.Logging
import zio.test.Assertion._
import zio.test.{TestConfig => _, _}
import zio.logging.slf4j.Slf4jLogger
import eu.timepit.refined.auto._
import zio.test.environment.TestEnvironment
import com.jerome.shortener.infrastructure.routes.UrlRoutes
import com.jerome.shortener.infrastructure.routes.circe.implicits._

object UrlRoutesSpec extends DefaultRunnableSpec {
  type UrlServiceLayer = UrlRepository with Config with Logging
  type UrlTask[A]      = RIO[UrlServiceLayer, A]

  implicit def circeJsonDecoder[A](implicit decoder: Decoder[A]): EntityDecoder[UrlTask, A] = jsonOf[UrlTask, A]
  implicit def circeJsonEncoder[A](implicit decoder: Encoder[A]): EntityEncoder[UrlTask, A] =
    jsonEncoderOf[UrlTask, A]

  private val urlService = UrlRoutes.routes[UrlServiceLayer].orNotFound

  override def spec: ZSpec[TestEnvironment, Any] = {
    suite("UrlServiceSpec unit tests")(
      testM("should shorten an url") {
        for {
          apiConfig <- Config.apiConfig
          result <- urlService.run(
            Request[UrlTask](method = Method.POST, uri = Uri.unsafeFromString("/"))
              .withEntity(UrlShortenRequest("http://www.nonexistingurl.com").asJson)
          )
          urlResponse <- result.as[UrlShortenedResponse]
          urlAlias = urlResponse.urlShortened.replace(s"http://${apiConfig.baseUrl}:${apiConfig.port}", "")
          url <- urlService.run(
            Request[UrlTask](
              method = Method.GET,
              uri = Uri.unsafeFromString(urlAlias)
            )
          )
        } yield
          assert(result)(hasField("status", _.status, equalTo(Status.Created))) &&
            assert(urlResponse)(
              equalTo(UrlShortenedResponse(s"http://${apiConfig.baseUrl}:${apiConfig.port}$urlAlias"))
            ) &&
            assert(url)(hasField("status", _.status, equalTo(Status.MovedPermanently))) &&
            assert(url)(
              hasField(
                "headers",
                _.headers,
                equalTo(Headers.of(Location(Uri.unsafeFromString("http://www.nonexistingurl.com"))))
              )
            )
      },
    ).provideCustomLayer(
      Slf4jLogger.make((_, msg) => msg) ++ TestConfig.test >+> TestUrlRepository.test
    )
  }

}
