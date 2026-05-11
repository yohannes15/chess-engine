package chessengine.domain

final case class GameState(
    board: Board,
    color: Color,
    captures: List[Piece] = Nil
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

    GameState(
      newBoard,
      color.opposite,
      newCaptures
    )
