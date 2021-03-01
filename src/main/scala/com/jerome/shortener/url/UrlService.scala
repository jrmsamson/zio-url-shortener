package com.jerome.shortener.url

import com.jerome.shortener.config.Config
import io.circe.{Decoder, Encoder}
import io.circe.generic.auto._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Location
import zio._
import zio.interop.catz._
import zio.logging.Logging

object UrlService {

  def routes[R <: UrlRepository with Config with Logging]: HttpRoutes[RIO[R, *]] = {
    type UrlTask[A] = RIO[R, A]

    implicit def circeJsonDecoder[A](implicit decoder: Decoder[A]): EntityDecoder[UrlTask, A] = jsonOf[UrlTask, A]
    implicit def circeJsonEncoder[A](implicit decoder: Encoder[A]): EntityEncoder[UrlTask, A] =
      jsonEncoderOf[UrlTask, A]

    val dsl: Http4sDsl[UrlTask] = Http4sDsl[UrlTask]
    import dsl._

    HttpRoutes.of[UrlTask] {
      case GET -> Root / urlAlias =>
        UrlRepository
          .get(UrlIdResolver(urlAlias).id)
          .tapCause { cause =>
            logging.log.error(s"Error getting url for the given url alias: $urlAlias") *>
              logging.log.error(cause.prettyPrint)
          }
          .foldM(
            {
              case _: UrlNotFound => BadRequest("There is no URL for the given alias")
              case _: Exception   => InternalServerError("Error getting url for the given alias")
            },
            url => {
              Task.succeed(
                Response[UrlTask]()
                  .withStatus(Status.MovedPermanently)
                  .withHeaders(Location(Uri.unsafeFromString(url.url)))
              )
            }
          )
      case request @ POST -> Root =>
        request
          .decode[UrlShortenRequest] { urlRequest =>
            (for {
              apiConfig <- Config.apiConfig
              url       <- UrlRepository.insert(urlRequest.url.value)
            } yield s"http://${apiConfig.baseUrl}:${apiConfig.port}/${UrlShorten(url).shorten}")
              .tapCause { cause =>
                logging.log.error(s"Error occurred minifying url: $urlRequest") *>
                  logging.log.error(cause.prettyPrint)
              }
              .foldM(
                _ => InternalServerError("Error minifying url"),
                urlAlias => Created(UrlShortenedResponse(urlAlias))
              )
          }
    }
  }
}
