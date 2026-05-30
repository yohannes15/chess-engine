package chessengine.domain

import cats.data.ValidatedNec
import cats.syntax.all.*
import chessengine.logic.MoveGenerator.{allLegalMoves, isSquareAttacked}

object PGN:
  private val tagsRegex = raw"""\[(\w+)\s*"([^"]*)"\]""".r
  private val moveNumRegex = raw"""\d+\.+""".r
  private val resultTokens = Set("1-0", "0-1", "1/2-1/2", "*")
  private val stripSuffixes = "[+#!?]+"

  def parse(s: String): ValidatedNec[String, PGNGame] =
    val sOneLine = s.trim.linesIterator.mkString(" ")
    val tagEnding = sOneLine.lastIndexOf("]")
    if tagEnding == -1 then
      "Invalid tags. No ']' char found".invalidNec
    else
      val (tagsStr, movesStr) = sOneLine.splitAt(tagEnding + 1)
      (parseTags(tagsStr), parseMoves(movesStr)).mapN(PGNGame.apply)

  /** Encode a move as SAN given the pre-move state */
  def toSAN(move: Move, state: GameState): String =

    def baseStr(m: Move): String =
      val pc = if m.piece.role == Role.Pawn then "" else m.piece.role.toSanChar
      val disambig = disambiguate(state, m)
      val captureX = if m.capture.isDefined then "x" else ""
      val dest = m.to.toNotation
      s"$pc$disambig$captureX$dest"

    val base = move match
      case m: CastlingMove  => if m.to.file == 6 then "O-O" else "O-O-O"
      case m: PromotionMove => s"${baseStr(m)}=${m.promotion.toSanChar}"
      case m                => baseStr(m)

    val nextState = state.applyMove(move)
    val kingSq = nextState.board.findPiece(nextState.color, Role.King)
    val inCheck = kingSq.exists(sq =>
      isSquareAttacked(nextState, sq, nextState.color.opposite)
    )
    val suffix = if !inCheck then ""
    else
      val isEmpty = allLegalMoves(nextState).isEmpty
      if isEmpty then "#" else "+"

    base + suffix

  private def parseTags(s: String): ValidatedNec[String, Map[String, String]] =
    tagsRegex.findAllMatchIn(s).map(m => m.group(1) -> m.group(2)).toMap match
      case m if m.nonEmpty => m.valid
      case _               => "No tags found".invalidNec

  /** Tokenize the move section and replay each SAN against a threaded
    * [[GameState]]. Fails fast on the first unresolvable token.
    */
  private def parseMoves(s: String): ValidatedNec[String, List[Move]] =
    val tokens = s.split("\\s+").map(_.trim).filterNot(t =>
      t.isEmpty || moveNumRegex.matches(t) || resultTokens.contains(t)
    ).toList

    tokens.foldLeft[Either[String, (GameState, List[Move])]](
      Right((GameState.initial, List.empty[Move]))
    ) {
      case (Right((state, acc)), token) =>
        val move = resolveSAN(token, state)
        move.map(mv => (state.applyMove(mv), mv :: acc))
      case (err, _) => err
    }.map(_._2.reverse).toValidatedNec

  /** Resolve a SAN token to a [[Move]] in the given position. Handles castling,
    * promotion, file/rank disambiguation, and annotation suffixes (`+`, `#`,
    * `!`, `?`). Returns `Left` if the SAN is illegal, ambiguous, or malformed.
    */
  def resolveSAN(san: String, state: GameState): Either[String, Move] =
    val stripped = san.replaceAll(stripSuffixes, "")

    if stripped == "O-O-O" then
      allLegalMoves(state)
        .collectFirst { case m: CastlingMove if m.to.file == 2 => m }
        .toRight(s"Queenside castling unavailable: $san")
    else if stripped == "O-O" then
      allLegalMoves(state)
        .collectFirst { case m: CastlingMove if m.to.file == 6 => m }
        .toRight(s"Kingside castling unavailable: $san")
    else
      val (sanNoPromo, promoRole) =
        if stripped.contains("=") then
          val Array(before, after) = stripped.split("=", 2)
          val role = Role.fromSanChar(after.head.toUpper)
            .filter(r => r != Role.King && r != Role.Pawn)
          (before, role)
        else (stripped, None)

      val cleaned = sanNoPromo.replace("x", "")

      val (role, rest) =
        if cleaned.headOption.exists(_.isUpper) then
          Role.fromSanChar(cleaned.head) match
            case Some(r) => (r, cleaned.tail)
            case None    => (Role.Pawn, cleaned)
        else (Role.Pawn, cleaned)

      if rest.length < 2 then
        Left(s"Cannot parse destination in: $san")
      else
        val destStr = rest.takeRight(2)
        val disambig = rest.dropRight(2)

        Square.fromNotation(
          destStr
        ).toRight(s"Invalid square '$destStr' in: $san").flatMap { dest =>
          val fileDisambig = disambig.find(c => c.isLetter && c.isLower)
          val rankDisambig = disambig.find(_.isDigit)

          val candidates = allLegalMoves(state).filter { m =>
            m.piece.role == role &&
            m.to == dest &&
            fileDisambig.forall(f => m.from.file == f - 'a') &&
            rankDisambig.forall(r => m.from.rank == r - '1') &&
            promoRole.forall { pr =>
              m match
                case pm: PromotionMove => pm.promotion == pr
                case _                 => false
            }
          }

          candidates match
            case single :: Nil => Right(single)
            case Nil           => Left(s"Illegal or no matching move: $san")
            case _ => Left(s"Ambiguous SAN (${candidates.size} matches): $san")
        }

  /** Minimal disambiguation prefix (file, rank, or both) needed to uniquely
    * identify `move.from` among all legal moves reaching the same square.
    */
  private def disambiguate(state: GameState, move: Move): String =
    val rivals = allLegalMoves(state).filter { m =>
      m.piece.role == move.piece.role &&
      m.to == move.to &&
      m.from != move.from
    }
    if rivals.isEmpty then ""
    else if rivals.forall(_.from.file != move.from.file) then
      ('a' + move.from.file).toChar.toString
    else if rivals.forall(_.from.rank != move.from.rank) then
      ('1' + move.from.rank).toChar.toString
    else
      ('a' + move.from.file).toChar.toString +
        ('1' + move.from.rank).toChar.toString
