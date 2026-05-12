package chessengine.domain

/** A Move "moves" a `piece` `from` -> `to` position. That is the basics of a
  * move. There are different types of moves:
  *   - Normal Move (regular move with potential capture)
  *   - Promotion Move (pawn move with potential capture and new role for pawn)
  *   - Castling move (king and castle move)
  */
sealed trait Move:
  def from: Square
  def to: Square
  def piece: Piece // moving piece
  def capture: Option[Piece]

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
