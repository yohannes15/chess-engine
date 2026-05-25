package chessengine

import chessengine.logic.MoveGenerator
import chessengine.domain.GameState

object TestHelpers:
  val startingFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
  val kiwiPeteFen =
    "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -"
  val position3Fen =
    "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -"
  val position4Fen =
    "r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1"
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
