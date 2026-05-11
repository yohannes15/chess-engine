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
  ): List[Move] =
    val (currRank, currFile) = (from.rank, from.file)

    val isStartingRank = piece.color match
      case Color.White => currRank == 1
      case Color.Black => currRank == 6

    val forward = piece.color match
      case Color.White => 1
      case Color.Black => -1

    // Forward 1 Move
    val forwardOne =
      Square.fromRankAndFile(currRank + forward, currFile).flatMap {
        sq =>
          if board.pieces(sq.index).isEmpty then
            Some(NormalMove(from, sq, piece, None))
          else
            None
      }
    // Forward 2 Move -> Only check if forwarDone was successful
    val forwardTwo = if isStartingRank && forwardOne.isDefined then
      Square.fromRankAndFile(currRank + 2 * forward, currFile).flatMap {
        sq =>
          if board.pieces(sq.index).isEmpty then
            Some(NormalMove(from, sq, piece, None))
          else None
      }
    else None
    // Captures
    val captures = List(currFile - 1, currFile + 1).flatMap { f =>
      Square.fromRankAndFile(currRank + forward, f).flatMap { sq =>
        board.pieces(sq.index) match
          case Some(target) if target.color != piece.color =>
            Some(NormalMove(from, sq, piece, Some(target)))
          case _ => None
      }
    }
    val potentialMoves: List[NormalMove] = (
      forwardOne.toList ++ forwardTwo.toList ++ captures.toList
    )
    // Check if potential moves are promotion
    // "For each move, if it's a promotion, give me 4 moves. otherwise, give me 1."
    val allMoves = potentialMoves.flatMap { move =>
      val isPromotion: Boolean =
        (piece.color == Color.White && move.to.rank == 7) ||
          (piece.color == Color.Black && move.to.rank == 0)

      if isPromotion then
        List(Role.Queen, Role.Rook, Role.Bishop, Role.Knight).map(role =>
          PromotionMove(move.from, move.to, move.piece, role, move.capture)
        )
      else
        List(move)
    }
    allMoves

  /** To know if a square is attacked by a color C. There are 2 ways
    *   - A) Generate all pseudo-legal moves for every `C` color piece and see
    *     if any of them land on the square.
    *   - B) TODO: Look outward from square.
    *     - If you look N, and see a C color Rook/Queen, its attacked.
    *     - If you look like a Knight and see a C color knight, its attacked.
    */
  def isSquareAttacked(
      board: Board,
      square: Square,
      attackerColor: Color
  ): Boolean =
    // Method A
    board.pieces.zipWithIndex.exists {
      case (Some(piece), idx) if piece.color == attackerColor =>
        pseudoLegalMovesFromSquare(board, Square.fromInt(idx).get)
          .exists(_.to.index == square.index)
      case _ => false
    }

  /** A move is legal if, after making the move, your own King is not attacked
    * by the Opponent
    */
  def isLegal(state: GameState, move: Move): Boolean =
    val currColor = move.piece.color
    val kingSquareAfterMove: Square = move.piece.role match
      case Role.King => move.to
      case _         =>
        state.board.pieces.zipWithIndex.collectFirst {
          case (Some(Piece(color, Role.King)), idx) if color == currColor =>
            Square.fromInt(idx).get
        }.get // safe because every valid board must have a king

    val newState = state.applyMove(move)

    !isSquareAttacked(newState.board, kingSquareAfterMove, currColor.opposite)
