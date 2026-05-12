package chessengine.logic

import chessengine.domain.*

object MoveGenerator:

  /** Every legal move available for the current player */
  def allLegalMoves(state: GameState): List[Move] =
    state.board.pieces.zipWithIndex.flatMap {
      case (Some(piece), idx) if piece.color == state.color =>
        legalMovesFromSquare(state, Square.fromInt(idx).get)
      case _ => Nil
    }.toList

  def legalMovesFromSquare(state: GameState, from: Square): List[Move] =
    pseudoLegalMovesFromSquare(
      state,
      from
    ).filter(move => isLegal(state, move))

  /** for attack detection primarily. You don't need to know if an opponent's
    * move is legal (i.e. if it leaves their King in check) to know if they are
    * attacking your King. If your king can be captured, you're in check!
    * period.
    */
  def pseudoLegalMovesFromSquare(state: GameState, from: Square): List[Move] =
    state.board.pieces(from.index) match
      case None        => List.empty[Move]
      case Some(piece) =>
        generateForPiece(state, from, piece)

  def generateForPiece(
      state: GameState,
      from: Square,
      piece: Piece
  ): List[Move] = piece.role match
    case r @ (Role.Rook | Role.Bishop | Role.Queen) =>
      r.moveOffsets.flatMap(dir => generateSliding(state, from, piece, dir))
    case r @ Role.Knight =>
      r.moveOffsets.flatMap(offset =>
        generateLeaping(state, from, piece, offset)
      )
    case r @ Role.King =>
      r.moveOffsets.flatMap(offset =>
        generateLeaping(state, from, piece, offset)
      ) ++ generateCastlingMoves(state, from, piece)
    case Role.Pawn =>
      generatePawnMoves(state, from, piece) ++
        generateEnPassantMoves(state, from, piece)

  def generateSliding(
      state: GameState,
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
          state.board.pieces(nextSq.index) match
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
      state: GameState,
      from: Square,
      piece: Piece,
      offset: (Int, Int)
  ): List[Move] =
    val nextRank = from.rank + offset(0)
    val nextFile = from.file + offset(1)
    Square.fromRankAndFile(nextRank, nextFile) match
      case Some(nextSq) =>
        state.board.pieces(nextSq.index) match
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
      state: GameState,
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
      Square.fromRankAndFile(currRank + forward, currFile).flatMap { sq =>
        if state.board.isEmptyAt(sq) then
          Some(NormalMove(from, sq, piece, None))
        else None
      }
    // Forward 2 Move -> Only check if forwarDone was successful
    val forwardTwo = if isStartingRank && forwardOne.isDefined then
      Square.fromRankAndFile(currRank + 2 * forward, currFile).flatMap { sq =>
        if state.board.isEmptyAt(sq) then
          Some(NormalMove(from, sq, piece, None))
        else None
      }
    else None
    // Captures
    val captures = List(currFile - 1, currFile + 1).flatMap { f =>
      Square.fromRankAndFile(currRank + forward, f).flatMap { sq =>
        state.board.pieces(sq.index) match
          case Some(target) if target.color != piece.color =>
            Some(NormalMove(from, sq, piece, Some(target)))
          case _ => None
      }
    }
    val potentialMoves: List[NormalMove] =
      forwardOne.toList ++ forwardTwo.toList ++ captures.toList

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
      else List(move)
    }
    allMoves

  /** Generate EnPassant moves if there is a vulnerable enPassantSquare and from
    * pawn can reach EnPassant Square
    */
  def generateEnPassantMoves(
      state: GameState,
      from: Square,
      piece: Piece
  ): List[Move] =
    state.enPassantSquare match
      case None       => Nil // There is no enPassantSquare to take over
      case Some(epSq) =>
        val forward = if piece.color == Color.White then 1 else -1
        val isDiagonal = epSq.rank == from.rank + forward &&
          Math.abs(epSq.file - from.file) == 1
        if isDiagonal then
          // The piece being captured is actually BEHIND the epSq
          val sqBehindEpSq = Square.fromRankAndFile(
            from.rank,
            epSq.file
          ).get
          val capturedPawn = state.board.pieces(sqBehindEpSq.index)
          List(EnPassantMove(from, epSq, piece, capturedPawn))
        else
          Nil

  /** Generate Castling moves for piece. The pre-conditions are:
    *   - Role: Is the piece a King?
    *   - Color: Is it White or Black?
    *   - Square: Is the king on its starting square (e1White and e8Black)
    *   - Rights: Does Gamestate say this color still has castling rights?
    */
  def generateCastlingMoves(
      state: GameState,
      from: Square,
      piece: Piece
  ): List[Move] =
    val r = if piece.color == Color.White then 1 else 8
    if from.toNotation != s"e$r" then Nil
    else
      val configs = List(
        // Side, pass, dest, rookFrom, rookTo, extraEmpty
        (Side.KingSide, s"f$r", s"g$r", s"h$r", s"f$r", Nil),
        (Side.QueenSide, s"d$r", s"c$r", s"a$r", s"d$r", List(s"b$r"))
      )

      configs.flatMap { case (side, pass, dest, rookFrom, rookTo, extra) =>
        val passSq = Square.fromNotation(pass).get
        val destSq = Square.fromNotation(dest).get
        val rookFromSq = Square.fromNotation(rookFrom).get
        val rookToSq = Square.fromNotation(rookTo).get
        val extraEmpty =
          extra.flatMap(Square.fromNotation).forall(state.board.isEmptyAt)

        if state.castlingRights.isAllowed(piece.color, side) &&
          canCastle(state, from, passSq, destSq, piece.color.opposite) &&
          extraEmpty
        then
          Some(
            CastlingMove(
              from = from,
              to = destSq,
              rookFrom = rookFromSq,
              rookTo = rookToSq,
              piece = piece
            )
          )
        else None
      }

  /** To know if a square is attacked by a color C. There are 2 ways
    *   - A) Generate all pseudo-legal moves for every `C` color piece and see
    *     if any of them land on the square.
    *   - B) TODO: Look outward from square.
    *     - If you look N, and see a C color Rook/Queen, its attacked.
    *     - If you look like a Knight and see a C color knight, its attacked.
    */
  def isSquareAttacked(
      state: GameState,
      square: Square,
      attackerColor: Color
  ): Boolean =
    state.board.pieces.zipWithIndex.exists {
      case (Some(piece), idx) if piece.color == attackerColor =>
        pseudoLegalMovesFromSquare(state, Square.fromInt(idx).get)
          .exists(_.to.index == square.index)
      case _ => false
    }

  /** A move is legal if,
    *   - after making the move, your own King is not attacked by the Opponent
    * A castling move is legal if
    *   - king hasn't moved at all
    *   - one of the two rooks haven't moved
    *   - empty space b/n king and one of the two rooks
    *   - currently not in check
    *   - moving spots not in check
    *   - ending spots not in check
    */
  def isLegal(state: GameState, move: Move): Boolean =
    val currColor = move.piece.color
    val kingSquareAfterMove: Square = move.piece.role match
      case Role.King => move.to
      case _         =>
        state.board.pieces.zipWithIndex
          .collectFirst {
            case (Some(Piece(color, Role.King)), idx) if color == currColor =>
              Square.fromInt(idx).get
          }
          .get // safe because every valid board must have a king

    val newState = state.applyMove(move)
    !isSquareAttacked(newState, kingSquareAfterMove, currColor.opposite)

  /** Given kingSq(starting), its passSq(passing square) and destSq(destination
    * square) and attackerColor, return whether the King can perform a castle
    */
  def canCastle(
      state: GameState,
      kingSq: Square,
      passSq: Square,
      destSq: Square,
      attackerColor: Color
  ): Boolean =
    val path = List(kingSq, passSq, destSq)
    val pathEmpty = state.board.isEmptyAt(passSq) &&
      state.board.isEmptyAt(destSq)
    val pathSafe =
      path.forall(sq => !isSquareAttacked(state, sq, attackerColor))
    pathEmpty && pathSafe

  def isCheckmate(state: GameState): Boolean =
    allLegalMoves(state).isEmpty && isSquareAttacked(
      state,
      state.board.findPiece(state.color, Role.King).get,
      state.color.opposite
    )

  def isStalemate(state: GameState): Boolean =
    allLegalMoves(state).isEmpty && !isSquareAttacked(
      state,
      state.board.findPiece(state.color, Role.King).get,
      state.color.opposite
    )
