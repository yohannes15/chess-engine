package chessengine.engine

/** Represents the nature of the score stored in the Transposition Table.
  *
  * | Type       | Condition            | Meaning                                               |
  * |:-----------|:---------------------|:------------------------------------------------------|
  * | Exact      | alpha < score < beta | The true value of the position at this depth.         |
  * | LowerBound | score >= beta        | A "Fail-High". The true value is at least this score. |
  * | UpperBound | score <= alpha       | A "Fail-Low". The true value is at most this score.   |
  */
enum ScoreType:
  case Exact, LowerBound, UpperBound
