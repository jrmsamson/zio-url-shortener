package com.jerome.shortener

import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.headers.Location
import org.http4s.implicits._
import zio._
import zio.interop.catz._
import zio.test.Assertion._
import zio.test.ZIOSpecDefault
import zio.test.{TestConfig => _, _}

object UrlRoutesSpec extends ZIOSpecDefault {
  type UrlServiceLayer = UrlRepository with AppConfig
  type UrlTask[A]      = RIO[UrlServiceLayer, A]

  implicit def circeJsonDecoder[A](implicit decoder: Decoder[A]): EntityDecoder[UrlTask, A] = jsonOf[UrlTask, A]

  private val urlService = UrlRoutes.routes[UrlServiceLayer].orNotFound

  override def spec: ZSpec[TestEnvironment, Any] =
    suite("UrlServiceSpec unit tests")(
      test("should shorten an url") {
        for {
          apiConfig <- AppConfig.apiConfig
          result <- urlService.run(
                      Request[UrlTask](method = Method.POST, uri = Uri.unsafeFromString("/"))
                        .withEntity(UrlShortenRequest("http://www.nonexistingurl.com").asJson)
                    )
          urlResponse <- result.as[UrlShortenedResponse]
          urlAlias     = urlResponse.urlShortened.replace(s"http://${apiConfig.baseUrl}:${apiConfig.port}", "")
          url <- urlService.run(
                   Request[UrlTask](
                     method = Method.GET,
                     uri = Uri.unsafeFromString(urlAlias)
                   )
                 )
        } yield assert(result)(hasField("status", _.status, equalTo(Status.Created))) &&
          assert(urlResponse)(
            equalTo(UrlShortenedResponse(s"http://${apiConfig.baseUrl}:${apiConfig.port}$urlAlias"))
          ) &&
          assert(url)(hasField("status", _.status, equalTo(Status.MovedPermanently))) &&
          assert(url)(
            hasField(
              "headers",
              _.headers,
              equalTo(Headers(Location(Uri.unsafeFromString("http://www.nonexistingurl.com"))))
            )
          )
      }
    ).provideCustomLayer(
      TestConfig.test >+> TestUrlRepository.test
    )

}
