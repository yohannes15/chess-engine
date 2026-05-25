package chessengine

import chessengine.logic.MoveGenerator
import chessengine.domain.GameState

object TestHelpers:
  val startingFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
  val startingHash = -5349250291650589088L

  /** counts leaf nodes from position(gs) after a ply of moves (depth).
    *
    * Start from a known position, enumerate every legal move to depth N, and
    * count the total number of nodes in the tree. Compare your count against a
    * known-correct reference value. If they match, move generation is correct
    * to that depth.
    */
  def perft(gs: GameState, depth: Int): Int =
    if depth == 0 then 1 // Leaf node; count this position
    else
      val moves = MoveGenerator.allLegalMoves(gs)
      moves.map(m => perft(gs.applyMove(m), depth - 1)).sum
