package com.jerome.shortener.model

sealed trait DomainError
sealed trait GetUrlRepositoryError extends DomainError
object GetUrlRepositoryError {
  final case class Error(exception: Throwable) extends GetUrlRepositoryError
  final case class UrlNotFound(id: UrlId)      extends GetUrlRepositoryError
}
final case class SaveUrlRepositoryError(exception: Throwable) extends DomainError
