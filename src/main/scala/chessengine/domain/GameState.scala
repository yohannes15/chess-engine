package chessengine.domain

import chessengine.engine.Zobrist

final case class GameState(
    board: Board,
    color: Color,
    captures: List[Piece],
    castlingRights: CastlingRights,
    enPassantSquare: Option[Square],
    hash: Long
):
  def applyMove(move: Move): GameState =
    val forward = if color == Color.White then 1 else -1
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
      case m: EnPassantMove =>
        val capturedSquare = Square.fromRankAndFile(
          m.to.rank - forward,
          m.to.file
        ).get
        board
          .update(m.from, None)
          .update(m.to, Some(m.piece))
          .update(capturedSquare, None)

    val nextEnPassantSquare = move match
      case m: NormalMove
          if m.piece.role == Role.Pawn &&
            Math.abs(m.from.rank - m.to.rank) == 2 =>
        Square.fromRankAndFile((m.from.rank + m.to.rank) / 2, m.from.file)
      case _ => None

    val nextCastlingRights = castlingRights.update(move)

    val nextHash = Zobrist.updateHash(
      hash,
      move,
      castlingRights,
      nextCastlingRights,
      enPassantSquare,
      nextEnPassantSquare
    )

    this.copy(
      board = newBoard,
      color = color.opposite,
      captures = newCaptures,
      castlingRights = nextCastlingRights,
      enPassantSquare = nextEnPassantSquare,
      hash = nextHash
    )

object GameState:
  def initial: GameState =
    val board = Board.initial
    val color = Color.White
    val rights = CastlingRights.initial
    val ep = None
    val hash = Zobrist.initialHash(board, color, rights, ep)
    GameState(board, color, Nil, rights, ep, hash)
