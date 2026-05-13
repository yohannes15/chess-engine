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

  def initialHash(
      board: Board,
      color: Color,
      castlingRights: CastlingRights,
      enPassantSquare: Option[Square]
  ): Long =
    val allPiecesOnBoardHash: Long =
      board.pieces.zipWithIndex.foldLeft(0L) {
        case (hash, (Some(piece), sqIdx)) =>
          val pieceSquareValue = pieceTable(sqIdx)(pieceIndex(piece))
          hash ^ pieceSquareValue
        case (hash, _) => hash
      }
    val sideToMoveHash: Long = color match
      case Color.White => 0L
      case Color.Black => sideToMove
    val castlingRightsHash: Long =
      castlingTable(castlingRightIndex(castlingRights))
    val enPassantHash: Long = enPassantSquare match
      case Some(sq) => enPassantTable(sq.file)
      case None     => 0L

    allPiecesOnBoardHash ^ sideToMoveHash ^ castlingRightsHash ^ enPassantHash

  def updateHash(
      oldHash: Long,
      move: Move,
      oldCastlingRights: CastlingRights,
      newCastlingRights: CastlingRights,
      oldEnPassantSquare: Option[Square],
      newEnPassantSquare: Option[Square]
  ): Long =
    val hashAfterPieces: Long = move match
      case m: NormalMove =>
        val pieceIdx = pieceIndex(m.piece)
        val h = oldHash ^ pieceTable(m.from.index)(pieceIdx) ^
          pieceTable(m.to.index)(pieceIdx)
        m.capture.fold(h)(victim =>
          h ^ pieceTable(m.to.index)(pieceIndex(victim))
        )

      case m: PromotionMove =>
        val pawnIdx = pieceIndex(m.piece)
        val promotedIdx = pieceIndex(Piece(m.piece.color, m.promotion))
        val h = oldHash ^ pieceTable(m.from.index)(pawnIdx) ^
          pieceTable(m.to.index)(promotedIdx)
        m.capture.fold(h)(victim =>
          h ^ pieceTable(m.to.index)(pieceIndex(victim))
        )

      case m: CastlingMove =>
        val kingIdx = pieceIndex(m.piece)
        val rookIdx = pieceIndex(Piece(m.piece.color, Role.Rook))
        oldHash ^
          pieceTable(m.from.index)(kingIdx) ^ pieceTable(m.to.index)(kingIdx) ^
          pieceTable(m.rookFrom.index)(rookIdx) ^
          pieceTable(m.rookTo.index)(rookIdx)

      case m: EnPassantMove =>
        val pawnIdx = pieceIndex(m.piece)
        // The victim is at (from.rank, to.file)
        val victimSq = Square.fromRankAndFile(m.from.rank, m.to.file).get
        val h = oldHash ^ pieceTable(m.from.index)(pawnIdx) ^
          pieceTable(m.to.index)(pawnIdx)

        m.capture.fold(h)(victim =>
          h ^ pieceTable(victimSq.index)(pieceIndex(victim))
        )

    val hashAfterSide = hashAfterPieces ^ sideToMove

    val hashAfterCastling = if oldCastlingRights != newCastlingRights then
      hashAfterSide ^ castlingTable(castlingRightIndex(oldCastlingRights)) ^
        castlingTable(castlingRightIndex(newCastlingRights))
    else hashAfterSide

    val hashAfterOldEP = oldEnPassantSquare.fold(hashAfterCastling)(sq =>
      hashAfterCastling ^ enPassantTable(sq.file)
    )
    val finalHash = newEnPassantSquare.fold(hashAfterOldEP)(sq =>
      hashAfterOldEP ^ enPassantTable(sq.file)
    )

    finalHash
