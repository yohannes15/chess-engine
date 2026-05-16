package chessengine.engine

import chessengine.domain.{GameState, Color, Square}

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
        val square = Square.fromInt(idx).get
        val pieceScore = Score(p.role.weight, PST.bonus(p, square))
        if p.color == Color.White then acc + pieceScore else acc - pieceScore
      case (acc, _) => acc
    }

  def materialBalance: Int = totalBalance.material
  def positionalBalance: Int = totalBalance.positional
