package chessengine.engine

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
