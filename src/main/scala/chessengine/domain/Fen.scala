package chessengine.domain

import cats.implicits.*
import chessengine.engine.Zobrist
import cats.data.ValidatedNec
import cats.data.Validated

/** FEN (Forsyth-Edwards Notation) is the standard way to describe a chess
  * position in a single line of text.
  */
object Fen:
  private final val validPieces =
    List('R', 'N', 'B', 'Q', 'K', 'P', 'r', 'n', 'b', 'q', 'k', 'p')

  def parse(fen: String): ValidatedNec[String, GameState] =
    val parts = fen.trim.split("\\s+").toSeq
    if parts.length != 4 && parts.length != 6 then
      s"Expected 4 or 6 FEN Fields. got ${parts.length}".invalidNec
    else
      val (pieces, side, castling, enP) =
        (parts(0), parts(1), parts(2), parts(3))
      val piecesParts = pieces.split("/").toList
      if piecesParts.length != 8 then
        s"""
          Expected 8 Piece fields separated with '/'. got ${piecesParts.length}
         """.invalidNec
      else
        (
          parseBoard(piecesParts),
          parseSide(side),
          parseRights(castling),
          parseEnPassant(enP)
        ).mapN((board, color, castlingRights, enPassantSquare) =>
          GameState(
            board,
            color,
            List.empty, // FEN does not track captured pieces
            castlingRights,
            enPassantSquare,
            Zobrist.initialHash(board, color, castlingRights, enPassantSquare)
          )
        )

  def write(state: GameState): String =
    ???

  /** 8 ranks, from rank 8 to rank 1. Digits 1-8 represent empty squares.
    * Example: [rnbqkbnr, pppppppp, 8, 8, 8, 8, PPPPPPPP, RNBQKBNR]
    */
  private def parseBoard(pieces: List[String]): ValidatedNec[String, Board] =
    pieces.reverse.traverse(parseRank).map(ranks =>
      Board(ranks.flatten.toVector)
    )

  private def parseRank(s: String)
      : ValidatedNec[String, Vector[Option[Piece]]] =
    if s.isEmpty then
      "Expected rank string in FEN format. got empty.".invalidNec
    else
      val rankWidth = s.foldLeft(0) {
        case (acc, c) if c.isDigit && c.asDigit >= 1 && c.asDigit <= 8 =>
          acc + c.asDigit
        case (acc, c) if validPieces.contains(c) => acc + 1
        case _                                   => 100 // Invalid character
      }

      if rankWidth != 8 then
        s"Invalid rank string (width $rankWidth): $s".invalidNec
      else
        s.foldLeft(Vector.empty[Option[Piece]]) {
          case (pieces, c) if c.isDigit =>
            pieces ++ Vector.fill(c.asDigit)(None)
          case (pieces, c) =>
            val piece = Piece(
              color = if c.isLower then Color.Black else Color.White,
              role = Role.fromChar(c.toLower)
            )
            pieces :+ Some(piece)
        }.validNec

  private def parseSide(s: String): ValidatedNec[String, Color] =
    s.toLowerCase match
      case "w" => Color.White.validNec
      case "b" => Color.Black.validNec
      case _ => s"Expected w/b for side values. got ${s.toLowerCase}".invalidNec

  private def parseEnPassant(s: String): ValidatedNec[String, Option[Square]] =
    s match
      case "-" => None.validNec
      case _   =>
        Validated
          .fromOption(Square.fromNotation(s), s"Invalid enPassantSquare: $s")
          .map(Some(_))
          .toValidatedNec

  private def parseRights(s: String): ValidatedNec[String, CastlingRights] =
    if (s == "-")
      CastlingRights(false, false, false, false).validNec
    else
      val chars = s.toSet
      val valid = Set('K', 'Q', 'k', 'q')
      if (!chars.subsetOf(valid))
        s"Invalid castling character in: $s".invalidNec
      else if (chars.size != s.length)
        s"Duplicate castling rights in: $s".invalidNec
      else
        CastlingRights(
          chars.contains('K'),
          chars.contains('Q'),
          chars.contains('k'),
          chars.contains('q')
        ).validNec
