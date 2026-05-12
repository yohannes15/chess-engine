package chessengine.engine

import chessengine.domain.{GameState, Color, Square}

/** Represents the components of a board evaluation.
  *
  * @param material
  *   The raw value of pieces on the board.
  * @param positional
  *   The bonus/penalty from Piece-Square Tables.
  */
case class Score(material: Int, positional: Int):
  def total: Int = material + positional
  def +(other: Score): Score =
    Score(material + other.material, positional + other.positional)
  def -(other: Score): Score =
    Score(material - other.material, positional - other.positional)
  def unary_- : Score = Score(-material, -positional)

object Score:
  val zero = Score(0, 0)

case class Evaluation(state: GameState):

  /** The final evaluation from the perspective of the current player. Positive
    * means the current player is winning.
    */
  def score: Int =
    val total = totalBalance.total
    if state.color == Color.White then total else -total

  /** The absolute balance (White - Black) broken down by components.
    */
  def totalBalance: Score =
    state.board.pieces.zipWithIndex.foldLeft(Score.zero) {
      case (acc, (Some(p), idx)) =>
        val pieceScore = Score(p.role.weight, PST.bonus(p, Square(idx)))
        if p.color == Color.White then acc + pieceScore else acc - pieceScore
      case (acc, _) => acc
    }

  def materialBalance: Int = totalBalance.material
  def positionalBalance: Int = totalBalance.positional
