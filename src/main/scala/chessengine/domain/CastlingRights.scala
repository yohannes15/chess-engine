package chessengine.domain

final case class CastlingRights(
    whiteKingSide: Boolean,
    whiteQueenSide: Boolean,
    blackKingSide: Boolean,
    blackQueenSide: Boolean
):
  def isAllowed(color: Color, side: Side): Boolean = (color, side) match
    case (Color.White, Side.KingSide)  => whiteKingSide
    case (Color.White, Side.QueenSide) => whiteQueenSide
    case (Color.Black, Side.KingSide)  => blackKingSide
    case (Color.Black, Side.QueenSide) => blackQueenSide

  def update(move: Move): CastlingRights =
    val rightsAfterMove = move match
      case CastlingMove(_, _, _, _, piece, _) =>
        piece.color match
          case Color.White =>
            this.copy(whiteKingSide = false, whiteQueenSide = false)
          case Color.Black =>
            this.copy(blackKingSide = false, blackQueenSide = false)

      case m: (NormalMove | PromotionMove) =>
        val withMovingPiece = m.piece match
          case Piece(Color.White, Role.King) =>
            this.copy(whiteKingSide = false, whiteQueenSide = false)
          case Piece(Color.Black, Role.King) =>
            this.copy(blackKingSide = false, blackQueenSide = false)
          case Piece(Color.White, Role.Rook) if m.from.toNotation == "a1" =>
            this.copy(whiteQueenSide = false)
          case Piece(Color.White, Role.Rook) if m.from.toNotation == "h1" =>
            this.copy(whiteKingSide = false)
          case Piece(Color.Black, Role.Rook) if m.from.toNotation == "a8" =>
            this.copy(blackQueenSide = false)
          case Piece(Color.Black, Role.Rook) if m.from.toNotation == "h8" =>
            this.copy(blackKingSide = false)
          case _ => this

        // Also check if a Rook was captured at its starting position
        m.to.toNotation match
          case "a1" => withMovingPiece.copy(whiteQueenSide = false)
          case "h1" => withMovingPiece.copy(whiteKingSide = false)
          case "a8" => withMovingPiece.copy(blackQueenSide = false)
          case "h8" => withMovingPiece.copy(blackKingSide = false)
          case _    => withMovingPiece

      case _ => this

    rightsAfterMove

object CastlingRights:
  def initial: CastlingRights = CastlingRights(true, true, true, true)
