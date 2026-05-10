package chessengine.domain

final case class Board private (pieces: Vector[Option[Piece]])

object Board:
  def empty: Board = Board(Vector.fill(64)(None))
  def initial: Board = ???
