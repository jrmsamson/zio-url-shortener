package com.jerome.shortener.domain

import zio._

package object repository {
  type UrlRepository = Has[UrlRepository.Service]
}
