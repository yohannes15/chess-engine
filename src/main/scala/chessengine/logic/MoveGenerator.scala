package chessengine.logic
import chessengine.domain.*

object MoveGenerator:
  def pseudoLegalMovesFromSquare(board: Board, from: Square): List[Move] =
    board.pieces(from.index) match
      case None        => List.empty[Move]
      case Some(piece) =>
        generateForPiece(board, from, piece)

  def generateForPiece(
      board: Board,
      from: Square,
      piece: Piece
  ): List[Move] = piece.role match
    case r @ (Role.Rook | Role.Bishop | Role.Queen) =>
      r.moveOffsets.flatMap(dir =>
        generateSliding(board, from, piece, dir)
      )
    case r @ (Role.Knight | Role.King) =>
      r.moveOffsets.flatMap(offset =>
        generateLeaping(board, from, piece, offset)
      )
    case Role.Pawn =>
      generatePawnMoves(board, from, piece)

  def generateSliding(
      board: Board,
      from: Square,
      piece: Piece,
      dir: (Int, Int)
  ): List[Move] =
    val (dr, df) = dir
    def walk(currRank: Int, currFile: Int): List[Move] =
      val nextRank = currRank + dr
      val nextFile = currFile + df
      Square.fromRankAndFile(nextRank, nextFile) match
        case None         => Nil // Edge of board
        case Some(nextSq) =>
          board.pieces(nextSq.index) match
            case None =>
              // Empty! Add move and Keep Walking
              NormalMove(from, nextSq, piece, None) :: walk(nextRank, nextFile)
            case Some(target) if target.color != piece.color =>
              // Opponent! Add capture and STOP
              List(NormalMove(from, nextSq, piece, Some(target)))
            case _ =>
              // Friend! STOP
              Nil
    walk(from.rank, from.file)

  def generateLeaping(
      board: Board,
      from: Square,
      piece: Piece,
      offset: (Int, Int)
  ): List[Move] =
    val nextRank = from.rank + offset(0)
    val nextFile = from.file + offset(1)
    Square.fromRankAndFile(nextRank, nextFile) match
      case Some(nextSq) =>
        board.pieces(nextSq.index) match
          case Some(target) if target.color != piece.color =>
            // Opponent! CAPTURE
            List(NormalMove(from, nextSq, piece, Some(target)))
          case None =>
            // Empty! Move to this spot
            List(NormalMove(from, nextSq, piece, None))
          case _ =>
            // Friend! STOP
            Nil

      case None => Nil // Edge of board

  def generatePawnMoves(
      board: Board,
      from: Square,
      piece: Piece
  ): List[Move] = ???
