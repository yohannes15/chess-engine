package chessengine.engine

import chessengine.domain.*
import scala.util.Random

/** There are 64 squares and 12 type of pieces (6 white, 6 black).So we need 768
  * random numbers.
  */
object Zobrist:
  private val random = Random(42)
  val pieceTable = Vector.fill(64, 12)(random.nextLong)
  // black / white
  val sideToMove = random.nextLong
  // all combinations of the 4 castling rights (2 * 2 * 2 * 2)
  val castlingTable = Vector.fill(16)(random.nextLong)
  // Only the file matters for EnPassant (8 files)
  val enPassantTable = Vector.fill(8)(random.nextLong)

  /** Get a unique value between 0 and 11 for a piece */
  def pieceIndex(p: Piece): Int =
    val colorIdx = if p.color == Color.White then 0 else 6
    val roleIdx = p.role match
      case Role.Pawn   => 0
      case Role.Knight => 1
      case Role.Bishop => 2
      case Role.Rook   => 3
      case Role.Queen  => 4
      case Role.King   => 5
    colorIdx + roleIdx

  /** Get a unique value between 0 and 15 for a Game State's castling rights
    * Treat the 4 booleans as bits.
    */
  def castlingRightIndex(rights: CastlingRights): Int =
    val wk = if rights.whiteKingSide then 1 else 0 // BIT 0
    val wq = if rights.whiteQueenSide then 2 else 0 // BIT 1
    val bk = if rights.blackKingSide then 4 else 0 // BIT 2
    val bq = if rights.blackQueenSide then 8 else 0 // BIT 3
    wk + wq + bk + bq

  def initialHash(state: GameState): Long =
    val allPiecesOnBoardHash: Long =
      state.board.pieces.zipWithIndex.foldLeft(0L) {
        case (hash, (Some(piece), sqIdx)) =>
          val pieceSquareValue = pieceTable(sqIdx)(pieceIndex(piece))
          hash ^ pieceSquareValue
        case (hash, _) => hash
      }
    val sideToMoveHash: Long = state.color match
      case Color.White => 0L
      case Color.Black => sideToMove
    val castlingRightsHash: Long =
      castlingTable(castlingRightIndex(state.castlingRights))
    val enPassantHash: Long = state.enPassantSquare match
      case Some(sq) => enPassantTable(sq.file)
      case None     => 0L

    allPiecesOnBoardHash ^ sideToMoveHash ^ castlingRightsHash ^ enPassantHash
