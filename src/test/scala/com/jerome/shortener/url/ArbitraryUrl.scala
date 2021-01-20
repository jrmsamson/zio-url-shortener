package com.jerome.shortener.url

import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Arbitrary.arbitrary

object ArbitraryUrl {
  implicit def arbitraryUrl: Arbitrary[Url] = Arbitrary {
    for {
      id  <- Gen.posNum[Int]
      url <- arbitrary[String]
    } yield Url(id, url)
  }
}
