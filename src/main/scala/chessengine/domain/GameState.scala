package chessengine.domain

final case class CastlingRights(
    whiteKingSide: Boolean = false,
    whiteQueenSide: Boolean = false,
    blackKingSide: Boolean = false,
    blackQueenSide: Boolean = false
):
  def update(move: Move): CastlingRights =
    move match
      case CastlingMove(_, _, _, _, piece, _) =>
        piece.color match
          case Color.White =>
            this.copy(whiteKingSide = false, whiteQueenSide = false)
          case Color.Black =>
            this.copy(blackKingSide = false, blackQueenSide = false)

      case m: (NormalMove | PromotionMove) =>
        val withMovingPiece = m.piece match
          case Piece(Color.White, Role.King) =>
            this.copy(whiteKingSide = false, whiteQueenSide = false)
          case Piece(Color.Black, Role.King) =>
            this.copy(blackKingSide = false, blackQueenSide = false)
          case Piece(Color.White, Role.Rook) if m.from.toNotation == "a1" =>
            this.copy(whiteQueenSide = false)
          case Piece(Color.White, Role.Rook) if m.from.toNotation == "h1" =>
            this.copy(whiteKingSide = false)
          case Piece(Color.Black, Role.Rook) if m.from.toNotation == "a8" =>
            this.copy(blackQueenSide = false)
          case Piece(Color.Black, Role.Rook) if m.from.toNotation == "h8" =>
            this.copy(blackKingSide = false)
          case _ => this

        // Also check if a Rook was captured at its starting position
        m.to.toNotation match
          case "a1" => withMovingPiece.copy(whiteQueenSide = false)
          case "h1" => withMovingPiece.copy(whiteKingSide = false)
          case "a8" => withMovingPiece.copy(blackQueenSide = false)
          case "h8" => withMovingPiece.copy(blackKingSide = false)
          case _    => withMovingPiece

final case class GameState(
    board: Board,
    color: Color,
    captures: List[Piece] = Nil,
    castlingRights: CastlingRights = CastlingRights()
):
  def applyMove(move: Move): GameState =
    val newCaptures = move.capture match
      case Some(p) => captures :+ p
      case None    => captures

    val newBoard = move match
      case m: NormalMove =>
        board.update(m.from, None).update(m.to, Some(m.piece))
      case m: PromotionMove =>
        board.update(
          m.from,
          None
        ).update(m.to, Some(Piece(m.piece.color, m.promotion)))
      case m: CastlingMove =>
        board
          .update(m.from, None)
          .update(m.to, Some(m.piece))
          .update(m.rookFrom, None)
          .update(m.rookTo, Some(Piece(m.piece.color, Role.Rook)))

    this.copy(
      board = newBoard,
      color = color.opposite,
      captures = newCaptures,
      castlingRights = castlingRights.update(move)
    )
