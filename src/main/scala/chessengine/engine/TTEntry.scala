package chessengine.engine

import chessengine.domain.{Move}

/** An entry of TranspositionTable
  * @param hash:
  *   The 64-bit Zobrist hash to verify the position (handles collisions).
  * @param depth:
  *   How deep the search was when this result was calculated.
  * @param score:
  *   The evaluation score.
  * @param scoreType:
  *   The "type" of score (Exact, Lower Bound, or Upper Bound).
  * @param bestMove:
  *   The move that produced the best score (used for move ordering).
  */
private final case class TTEntry(
    hash: Long,
    depth: Int,
    score: Int,
    scoreType: ScoreType,
    bestMove: Move
)
