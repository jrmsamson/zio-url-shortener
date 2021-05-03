package com.jerome.shortener.infrastructure

import doobie._
import eu.timepit.refined.api.Refined
import eu.timepit.refined._

package object repository {
  object refined {
    implicit val putRefinedUrl: Put[String Refined string.Url] = Put[String].contramap(_.value)
    implicit val getRefinedUrl: Get[String Refined string.Url] = Get[String].temap(refineV[string.Url](_))
  }
}
