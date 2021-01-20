package com.jerome.shortener.url

import eu.timepit.refined.api.Refined
import eu.timepit.refined.string

import scala.annotation.tailrec

final case class Url(id: Int, url: String)
final case class UrlNotFound(id: Int) extends Exception
final case class UrlShortenRequest(url: String Refined string.Url)
final case class UrlShortenedResponse(urlShortened: String)

object Url {
  private val ModBase62 = 62
  private val IntModBase62ToLetterMap: Map[Int, String] = {
    // Index [1-62] inclusive
    (0 to 25).map(index => (index, (index + 97).toChar.toString)).toMap ++        // [a-z]
      (0 to 25).map(index => (index + 26, (index + 65).toChar.toString)).toMap ++ // [A-Z]
      (0 to 9).map(index => (index + 52, index.toString)).toMap // [0-9]
  }
  private val LetterToIntModBase62: Map[String, Int] = IntModBase62ToLetterMap.map {
    case (intModBase62, letter) => letter -> intModBase62
  }
  // Alias = [a-zA-Z0-9]+
  def generateUrlAlias(url: Url): String = {
    @tailrec
    def loop(num: Int, accu: Seq[Int]): Seq[Int] = {
      if (num > 0) loop(num / ModBase62, (num % ModBase62) +: accu)
      else accu
    }
    loop(url.id, Seq.empty)
      .flatMap(IntModBase62ToLetterMap.get)
      .mkString
  }

  def resolveUrlId(urlAlias: String): Int =
    urlAlias
      .flatMap(letter => LetterToIntModBase62.get(letter.toString))
      .reverse
      .zipWithIndex
      .foldLeft(0) {
        case (urlId, (intModBase62, base62Exp)) =>
          intModBase62 * math.pow(ModBase62.toDouble, base62Exp.toDouble).toInt + urlId
      }

}
