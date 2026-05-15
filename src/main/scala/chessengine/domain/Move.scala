package chessengine.domain

/** A Move "moves" a `piece` `from` -> `to` position. That is the basics of a
  * move. There are different types of moves:
  *   - Normal Move (regular move with potential capture)
  *   - Promotion Move (pawn move with potential capture and new role for pawn)
  *   - Castling move (king and castle move)
  *   - EnPassant move (special rare scenario pawn move with odd capture)
  */
sealed trait Move:
  def from: Square
  def to: Square
  def piece: Piece // moving piece
  def capture: Option[Piece]

  private final val _UCI: String = s"${from.toNotation}${to.toNotation}"

  def toUCI: String =
    this match
      case m: PromotionMove => _UCI + m.promotion.toUci
      case _                => _UCI

case class NormalMove(
    from: Square,
    to: Square,
    piece: Piece,
    capture: Option[Piece]
) extends Move

case class PromotionMove(
    from: Square,
    to: Square,
    piece: Piece,
    promotion: Role,
    capture: Option[Piece]
) extends Move

case class CastlingMove(
    from: Square,
    to: Square,
    rookFrom: Square,
    rookTo: Square,
    piece: Piece,
    capture: Option[Piece] = None
) extends Move

case class EnPassantMove(
    from: Square,
    to: Square,
    piece: Piece,
    capture: Option[Piece]
) extends Move
