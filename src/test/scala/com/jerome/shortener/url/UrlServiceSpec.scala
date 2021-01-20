package com.jerome.shortener.url

import com.jerome.shortener.config.Config
import eu.timepit.refined.string
import io.circe.{Decoder, Encoder}
import org.http4s._
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.headers.Location
import org.http4s.implicits._
import zio.{RIO, ZEnv}
import zio.interop.catz._
import zio.logging.Logging
import zio.test.Assertion._
import zio.test.{TestConfig => _, _}
import zio.logging.slf4j.Slf4jLogger
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._

object UrlServiceSpec extends DefaultRunnableSpec {
  type UrlServiceLayer = UrlRepository with Config with Logging
  type UrlTask[A]      = RIO[UrlServiceLayer, A]

  implicit def circeJsonDecoder[A](implicit decoder: Decoder[A]): EntityDecoder[UrlTask, A] = jsonOf[UrlTask, A]
  implicit def circeJsonEncoder[A](implicit decoder: Encoder[A]): EntityEncoder[UrlTask, A] =
    jsonEncoderOf[UrlTask, A]

  private val urlService = UrlService.routes[UrlServiceLayer].orNotFound

  override def spec = {
    suite("UrlServiceSpec unit tests")(
      testM("should shorten an url") {
        val urlMinified: String Refined string.Url = "http://www.nonexistingurl.com"
        for {
          apiConfig <- Config.apiConfig
          result <- urlService.run(
            Request[UrlTask](method = Method.POST, uri = Uri.unsafeFromString("/"))
              .withEntity(UrlShortenRequest(urlMinified).asJson)
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
          assert(result.status)(equalTo(Status.Created)) &&
            assert(urlResponse.urlShortened)(equalTo(s"http://${apiConfig.baseUrl}:${apiConfig.port}$urlAlias")) &&
            assert(url.status)(equalTo(Status.MovedPermanently)) &&
            assert(url.headers)(equalTo(Headers.of(Location(Uri.unsafeFromString(s"$urlMinified")))))
      },
    ).provideSomeLayer[ZEnv](
      Slf4jLogger.make((_, msg) => msg) ++ TestConfig.test >+> TestUrlRepository.test
    )
  }

}
