package chessengine.domain

import cats.data.ValidatedNec
import cats.syntax.all.*

object PGN:
  /** - (\w+) → key (Event, Date, etc.)   - ([^"]*) → value inside quotes */
  private val tagsRegex = raw"""\[(\w+)\s*"([^"]*)"\]""".r

  def parse(s: String): ValidatedNec[String, PGNGame] =
    val sOneLine = s.trim.linesIterator.mkString(" ")
    val tagEnding = sOneLine.lastIndexOf("]")
    if tagEnding == -1 then
      "Invalid tags. No ']' char found".invalidNec
    else
      val tagsStr = sOneLine.slice(0, tagEnding + 1)
      val tags = parseTags(tagsStr)
      val moves = parseMoves(sOneLine.slice(tagEnding + 1, sOneLine.size))
      (tags, moves).mapN(PGNGame.apply)

  private def parseTags(s: String): ValidatedNec[String, Map[String, String]] =
    tagsRegex.findAllMatchIn(s).map(m => m.group(1) -> m.group(2)).toMap match
      case m if !m.isEmpty => m.valid
      case _               => "No tags found".invalidNec

  private def parseMoves(s: String): ValidatedNec[String, List[Move]] =
    ???
