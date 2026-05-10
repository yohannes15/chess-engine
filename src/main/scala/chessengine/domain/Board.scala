package chessengine.domain

final case class Board private (pieces: Vector[Option[Piece]])

object Board:
  def empty: Board = ???
  def initial: Board = ???
