package chessengine.engine

import chessengine.domain.*

/** Piece-Square Tables (PST) provide positional bonuses/penalties for pieces.
  * These values are based on the "Simplified Evaluation Function" by Tomasz
  * Michniewski.
  * @see
  *   [[https://www.chessprogramming.org/Simplified_Evaluation_Function]]
  *
  * Tables are defined from White's perspective. Index 0 is a1, Index 63 is h8.
  */
object PST:

  def bonus(role: Role, square: Square, color: Color): Int =
    val idx = if color == Color.White then square.index else square.flip
    role match
      case Role.Pawn   => pawnTable(idx)
      case Role.Knight => knightTable(idx)
      case Role.Bishop => bishopTable(idx)
      case Role.Rook   => rookTable(idx)
      case Role.Queen  => queenTable(idx)
      case Role.King   => kingTable(idx)

  private val pawnTable = Vector(
    0, 0, 0, 0, 0, 0, 0, 0, // Rank 1
    5, 10, 10, -20, -20, 10, 10, 5, // Rank 2
    5, -5, -10, 0, 0, -10, -5, 5, // Rank 3
    0, 0, 0, 20, 20, 0, 0, 0, // Rank 4
    5, 5, 10, 25, 25, 10, 5, 5, // Rank 5
    10, 10, 20, 30, 30, 20, 10, 10, // Rank 6
    50, 50, 50, 50, 50, 50, 50, 50, // Rank 7
    0, 0, 0, 0, 0, 0, 0, 0 // Rank 8
  )

  private val knightTable = Vector(
    -50, -40, -30, -30, -30, -30, -40, -50, // Rank 1
    -40, -20, 0, 5, 5, 0, -20, -40, // Rank 2
    -30, 5, 10, 15, 15, 10, 5, -30, // Rank 3
    -30, 0, 15, 20, 20, 15, 0, -30, // Rank 4
    -30, 5, 15, 20, 20, 15, 5, -30, // Rank 5
    -30, 0, 10, 15, 15, 10, 0, -30, // Rank 6
    -40, -20, 0, 0, 0, 0, -20, -40, // Rank 7
    -50, -40, -30, -30, -30, -30, -40, -50 // Rank 8
  )

  private val bishopTable = Vector(
    -20, -10, -10, -10, -10, -10, -10, -20, // Rank 1
    -10, 5, 0, 0, 0, 0, 5, -10, // Rank 2
    -10, 10, 10, 10, 10, 10, 10, -10, // Rank 3
    -10, 0, 10, 10, 10, 10, 0, -10, // Rank 4
    -10, 5, 5, 10, 10, 5, 5, -10, // Rank 5
    -10, 0, 5, 10, 10, 5, 0, -10, // Rank 6
    -10, 0, 0, 0, 0, 0, 0, -10, // Rank 7
    -20, -10, -10, -10, -10, -10, -10, -20 // Rank 8
  )

  private val rookTable = Vector(
    0, 0, 0, 5, 5, 0, 0, 0, // Rank 1
    -5, 0, 0, 0, 0, 0, 0, -5, // Rank 2
    -5, 0, 0, 0, 0, 0, 0, -5, // Rank 3
    -5, 0, 0, 0, 0, 0, 0, -5, // Rank 4
    -5, 0, 0, 0, 0, 0, 0, -5, // Rank 5
    -5, 0, 0, 0, 0, 0, 0, -5, // Rank 6
    5, 10, 10, 10, 10, 10, 10, 5, // Rank 7
    0, 0, 0, 0, 0, 0, 0, 0 // Rank 8
  )

  private val queenTable = Vector(
    -20, -10, -10, -5, -5, -10, -10, -20, // Rank 1
    -10, 0, 5, 0, 0, 0, 0, -10, // Rank 2
    -10, 5, 5, 5, 5, 5, 0, -10, // Rank 3
    0, 0, 5, 5, 5, 5, 0, -5, // Rank 4
    -5, 0, 5, 5, 5, 5, 0, -5, // Rank 5
    -10, 0, 5, 5, 5, 5, 0, -10, // Rank 6
    -10, 0, 0, 0, 0, 0, 0, -10, // Rank 7
    -20, -10, -10, -5, -5, -10, -10, -20 // Rank 8
  )

  private val kingTable = Vector(
    20, 30, 10, 0, 0, 10, 30, 20, // Rank 1
    20, 20, 0, 0, 0, 0, 20, 20, // Rank 2
    -10, -20, -20, -20, -20, -20, -20, -10, // Rank 3
    -20, -30, -30, -40, -40, -30, -30, -20, // Rank 4
    -30, -40, -40, -50, -50, -40, -40, -30, // Rank 5
    -30, -40, -40, -50, -50, -40, -40, -30, // Rank 6
    -30, -40, -40, -50, -50, -40, -40, -30, // Rank 7
    -30, -40, -40, -50, -50, -40, -40, -30 // Rank 8
  )
