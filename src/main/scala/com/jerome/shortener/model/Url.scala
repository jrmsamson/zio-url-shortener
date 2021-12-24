package com.jerome.shortener.model

import doobie.util.Get

import java.net.URI
import scala.annotation.tailrec
import scala.util.Try
import scala.util.chaining._

final case class Url(id: UrlId, url: URI)

object Url {
  implicit val uriGet: Get[URI] = Get[String].temap(str => Try(new URI(str)).toEither.left.map(_.getMessage()))
}

final case class UrlId(value: Int) extends AnyVal {
  import UrlId._

  // Alias = [a-zA-Z0-9]+
  def generateAlias: String = {
    @tailrec
    def loop(num: Int, accu: Seq[Int]): Seq[Int] =
      if (num > 0) loop(num / ModBase62, (num % ModBase62) +: accu)
      else accu

    loop(value, Seq.empty)
      .flatMap(IntModBase62ToLetterMap.get)
      .mkString
  }
}

object UrlId {
  val ModBase62 = 62
  val IntModBase62ToLetterMap: Map[Int, String] = {
    // Index [1-62] inclusive
    (0 to 25).map(index => (index, (index + 97).toChar.toString)).toMap ++        // [a-z]
      (0 to 25).map(index => (index + 26, (index + 65).toChar.toString)).toMap ++ // [A-Z]
      (0 to 9).map(index => (index + 52, index.toString)).toMap                   // [0-9]
  }
  val LetterToIntModBase62: Map[String, Int] = IntModBase62ToLetterMap.map { case (intModBase62, letter) =>
    letter -> intModBase62
  }

  def fromAlias(urlAlias: String): UrlId =
    urlAlias
      .flatMap(letter => LetterToIntModBase62.get(letter.toString))
      .reverse
      .zipWithIndex
      .foldLeft(0) { case (urlId, (intModBase62, base62Exp)) =>
        intModBase62 * math.pow(ModBase62.toDouble, base62Exp.toDouble).toInt + urlId
      }
      .pipe(new UrlId(_))
}
