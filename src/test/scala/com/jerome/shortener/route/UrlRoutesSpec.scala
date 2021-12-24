package com.jerome.shortener.route

import com.jerome.shortener.TestConfig
import com.jerome.shortener.TestUrlRepository
import com.jerome.shortener.config.AppConfig
import com.jerome.shortener.model.UrlShortenRequest
import com.jerome.shortener.model.UrlShortenedResponse
import com.jerome.shortener.repository.UrlRepository
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
import java.net.URI

object UrlRoutesSpec extends ZIOSpecDefault {
  type UrlServiceLayer = UrlRepository with AppConfig
  type UrlTask[A]      = RIO[UrlServiceLayer, A]

  implicit def circeJsonDecoder[A](implicit decoder: Decoder[A]): EntityDecoder[UrlTask, A] = jsonOf[UrlTask, A]

  private val urlRoutes = UrlRoutes.routes[UrlServiceLayer].orNotFound

  override def spec: ZSpec[TestEnvironment, Any] =
    suite("UrlRoutes specs")(
      test("should shorten an url") {
        for {
          apiConfig <- AppConfig.getApiConfig
          result <- urlRoutes.run(
                      Request[UrlTask](method = Method.POST, uri = Uri.unsafeFromString("/"))
                        .withEntity(UrlShortenRequest(new URI("http://www.nonexistingurl.com")).asJson)
                    )
          _           <- ZIO.succeed(println(s"RESULT ${result.status}"))
          urlResponse <- result.as[UrlShortenedResponse]
          urlAlias     = urlResponse.urlShortened.replace(s"http://${apiConfig.baseUrl}:${apiConfig.port}", "")
          url <- urlRoutes.run(
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
    ).provide(
      TestConfig.test,
      TestUrlRepository.test
    )

}
