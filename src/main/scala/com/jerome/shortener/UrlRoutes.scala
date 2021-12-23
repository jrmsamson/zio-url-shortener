package com.jerome.shortener

import com.jerome.shortener.GetUrlRepositoryError._
import eu.timepit.refined._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
import eu.timepit.refined.predicates.all.NonEmpty
import eu.timepit.refined.refineV
import io.circe._
import io.circe.generic.auto._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Location
import zio._
import zio.interop.catz._
import zio.logging._

object UrlRoutes {

  def routes[R <: Has[UrlRepository] with Has[AppConfig] with Logging]: HttpRoutes[RIO[R, *]] = {
    type UrlTask[A] = RIO[R, A]

    implicit def circeJsonDecoder[A](implicit decoder: Decoder[A]): EntityDecoder[UrlTask, A] =
      jsonOf[UrlTask, A]

    implicit def circeJsonEncoder[A](implicit decoder: Encoder[A]): EntityEncoder[UrlTask, A] =
      jsonEncoderOf[UrlTask, A]

    val dsl: Http4sDsl[UrlTask] = Http4sDsl[UrlTask]
    import dsl._

    HttpRoutes.of[UrlTask] {
      case GET -> Root / urlAlias =>
        refineV[NonEmpty](urlAlias).fold(
          error => BadRequest(error),
          urlAlias =>
            UrlRepository
              .get(Url.idFromAlias(urlAlias))
              .foldM(
                {
                  case UrlNotFound(_) =>
                    BadRequest(s"There is no URL for the given alias: $urlAlias")
                  case GetUrlRepositoryError.Error(exception) =>
                    log.error(s"Error getting url for the given alias: $exception") *>
                      InternalServerError("Error getting url for the given alias")
                },
                url =>
                  Task.succeed(
                    Response[UrlTask]()
                      .withStatus(Status.MovedPermanently)
                      .withHeaders(Location(Uri.unsafeFromString(url.url)))
                  )
              )
        )

      case request @ POST -> Root =>
        request
          .decode[UrlShortenRequest] { urlRequest =>
            (for {
              apiConfig <- AppConfig.apiConfig
              url       <- UrlRepository.save(urlRequest.url)
            } yield s"http://${apiConfig.baseUrl}:${apiConfig.port}/${url.shorten}")
              .foldM(
                { case SaveUrlRepositoryError(exception) =>
                  log.error(s"Error minifying url $urlRequest: $exception") *>
                    InternalServerError("Error minifying url")
                },
                urlAlias => Created(UrlShortenedResponse(urlAlias))
              )
          }
    }
  }
}
