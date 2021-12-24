package com.jerome.shortener.route

import com.jerome.shortener.config.AppConfig
import com.jerome.shortener.model.GetUrlRepositoryError
import com.jerome.shortener.model.GetUrlRepositoryError.UrlNotFound
import com.jerome.shortener.model.SaveUrlRepositoryError
import com.jerome.shortener.model.Url
import com.jerome.shortener.model.UrlId
import com.jerome.shortener.model.UrlShortenRequest
import com.jerome.shortener.model.UrlShortenedResponse
import com.jerome.shortener.repository.UrlRepository
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Location
import zio._
import zio.interop.catz._
import zio.interop.catz.implicits._

object UrlRoutes {

  def routes[R <: UrlRepository with AppConfig]: HttpRoutes[RIO[R, *]] = {
    type UrlTask[A] = RIO[R, A]

    implicit def circeJsonDecoder[A](implicit decoder: Decoder[A]): EntityDecoder[UrlTask, A] =
      jsonOf[UrlTask, A]

    val dsl: Http4sDsl[UrlTask] = Http4sDsl[UrlTask]
    import dsl._

    HttpRoutes.of[UrlTask] {
      case GET -> Root / urlAlias =>
        UrlRepository
          .get(UrlId.fromAlias(urlAlias))
          .foldZIO(
            {
              case UrlNotFound(_) =>
                BadRequest(s"There is no URL for the given alias: $urlAlias")
              case GetUrlRepositoryError.Error(exception) =>
                ZIO.logError(s"Error getting url for the given alias: $exception") *>
                  InternalServerError("Error getting url for the given alias")
            },
            urlShorten =>
              Task.succeed(
                Response[UrlTask]()
                  .withStatus(Status.MovedPermanently)
                  .withHeaders(Location(Uri.unsafeFromString(urlShorten.url.toString())))
              )
          )

      case request @ POST -> Root =>
        request
          .decode[UrlShortenRequest] { urlRequest =>
            (for {
              apiConfig <- AppConfig.getApiConfig
              url       <- UrlRepository.save(urlRequest.url)
            } yield s"http://${apiConfig.baseUrl}:${apiConfig.port}/${url.id.generateAlias}")
              .foldZIO(
                { case SaveUrlRepositoryError(exception) =>
                  ZIO.logError(s"Error minifying url $urlRequest: $exception") *>
                    InternalServerError("Error minifying url")
                },
                urlAlias => Created(UrlShortenedResponse(urlAlias).asJson)
              )
          }

    }
  }
}
