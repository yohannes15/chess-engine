package chessengine.domain

final case class Board private (pieces: Vector[Option[Piece]]):
  def update(s: Square, p: Option[Piece]): Board =
    Board(pieces.updated(s.index, p))

  def isEmptyAt(s: Square): Boolean =
    pieces(s.index) match
      case Some(piece) => false
      case None        => true

  def findPiece(color: Color, role: Role): Option[Square] =
    pieces.zipWithIndex.collectFirst({
      case (Some(Piece(c, r)), idx) if c == color && r == role =>
        Square.fromInt(idx).get
    })

object Board:
  import Color.*
  import Role.*

  private def at(notation: String, color: Color, role: Role): (Square, Piece) =
    Square.fromNotation(notation).get -> Piece(color, role)

  private final val startingPositions: Map[Square, Piece] = Map(
    // White
    at("a1", White, Rook),
    at("b1", White, Knight),
    at("c1", White, Bishop),
    at("d1", White, Queen),
    at("e1", White, King),
    at("f1", White, Bishop),
    at("g1", White, Knight),
    at("h1", White, Rook),
    at("a2", White, Pawn),
    at("b2", White, Pawn),
    at("c2", White, Pawn),
    at("d2", White, Pawn),
    at("e2", White, Pawn),
    at("f2", White, Pawn),
    at("g2", White, Pawn),
    at("h2", White, Pawn),
    // Black
    at("a7", Black, Pawn),
    at("b7", Black, Pawn),
    at("c7", Black, Pawn),
    at("d7", Black, Pawn),
    at("e7", Black, Pawn),
    at("f7", Black, Pawn),
    at("g7", Black, Pawn),
    at("h7", Black, Pawn),
    at("a8", Black, Rook),
    at("b8", Black, Knight),
    at("c8", Black, Bishop),
    at("d8", Black, Queen),
    at("e8", Black, King),
    at("f8", Black, Bishop),
    at("g8", Black, Knight),
    at("h8", Black, Rook)
  )

  def empty: Board = Board(Vector.fill(64)(None))

  def initial: Board =
    startingPositions.foldLeft(Board.empty)({
      case (board, (sq, piece)) => board.update(sq, Some(piece))
    })
