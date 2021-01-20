package com.jerome.shortener.url

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import ArbitraryUrl._

class UrlSpec extends AnyFlatSpec with Matchers with ScalaCheckPropertyChecks {
  it should "generate url alias" in {
    forAll(minSuccessful(10000)) { url: Url =>
      val urlAlias = Url.generateUrlAlias(url)
      assert(Url.resolveUrlId(urlAlias) == url.id)
    }
  }
}
