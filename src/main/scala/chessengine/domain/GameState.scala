package chessengine.domain

import chessengine.engine.Zobrist

enum Side:
  case KingSide, QueenSide

final case class CastlingRights(
    whiteKingSide: Boolean = false,
    whiteQueenSide: Boolean = false,
    blackKingSide: Boolean = false,
    blackQueenSide: Boolean = false
):
  def isAllowed(color: Color, side: Side): Boolean = (color, side) match
    case (Color.White, Side.KingSide)  => whiteKingSide
    case (Color.White, Side.QueenSide) => whiteQueenSide
    case (Color.Black, Side.KingSide)  => blackKingSide
    case (Color.Black, Side.QueenSide) => blackQueenSide

  def update(move: Move): CastlingRights =
    val rightsAfterMove = move match
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

      case _ => this

    rightsAfterMove

final case class GameState(
    board: Board,
    color: Color,
    captures: List[Piece] = Nil,
    castlingRights: CastlingRights = CastlingRights(),
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
    val rights = CastlingRights(true, true, true, true)
    val ep = None
    val hash = Zobrist.initialHash(board, color, rights, ep)
    GameState(board, color, Nil, rights, ep, hash)
