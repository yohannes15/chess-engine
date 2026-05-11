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
  ): List[Move] = ???

  def generateLeaping(
      board: Board,
      from: Square,
      piece: Piece,
      offset: (Int, Int)
  ): List[Move] = ???

  def generatePawnMoves(
      board: Board,
      from: Square,
      piece: Piece
  ): List[Move] = ???
