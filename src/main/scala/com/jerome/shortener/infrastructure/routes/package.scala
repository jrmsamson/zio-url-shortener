package com.jerome.shortener.infrastructure

import eu.timepit.refined.api.Refined
import eu.timepit.refined._
import io.circe._

package object routes {
  object circe {
    object implicits {
      implicit val stringRefinedUrlDecoder: Decoder[String Refined string.Url] =
        Decoder.decodeString.emap(refineV[string.Url](_))

      implicit val stringRefinedUrlEncoder: Encoder[String Refined string.Url] =
        Encoder.encodeString.contramap[String Refined string.Url](_.value)
    }
  }
}
