package chessengine.domain

enum Role:
  case Pawn, Rook, Knight, Bishop, Queen, King

  def weight: Int =
    this match
      case Pawn   => 100
      case Knight => 320
      case Bishop => 330
      case Rook   => 500
      case Queen  => 900
      case King   => 20000

  def isSliding: Boolean = this match
    case Rook | Bishop | Queen => true
    case _                     => false

  def isLeaping: Boolean = this match
    case Knight | King => true
    case _             => false

  def moveOffsets: List[(Int, Int)] = this match
    case Rook   => rookOffsets
    case Knight => knightOffsets
    case Bishop => bishopOffsets
    case Queen  => queenOffsets
    case King   => kingOffsets
    case _      => List.empty

  private val rookOffsets = List(
    (0, 1), // North
    (0, -1), // South
    (1, 0), // East
    (-1, 0) // West
  )
  private val bishopOffsets = List(
    (1, 1), // NE
    (1, -1), // SE
    (-1, 1), // NW
    (-1, -1) // SW
  )
  private lazy val queenOffsets = rookOffsets ++ bishopOffsets
  private val knightOffsets = List(
    (1, 2), // NE
    (1, -2), // SE
    (-1, 2), // NW
    (-1, -2), // SW
    (2, 1), // NE
    (2, -1), // SE
    (-2, 1), // NW
    (-2, -1) // SW
  )
  private val kingOffsets = List(
    (0, 1), // N
    (0, -1), // S
    (1, 0), // E
    (-1, 0), // W
    (1, 1), // NE
    (1, -1), // SE
    (-1, 1), // NW
    (-1, -1) // SW
  )
