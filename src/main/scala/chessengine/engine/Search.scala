package chessengine.engine

import chessengine.domain.*
import chessengine.logic.MoveGenerator.{allLegalMoves, isCheckmate, isStalemate}
import ScoreType.*

class Search(tt: TranspositionTable):

  private val MateScore = 1000000
  private val StalemateScore = 0

  /** Finds the best move for the current player using a Negamax search with
    * Alpha-Beta pruning and Transposition Table optimizations.
    *
    * @param state
    *   The current game state.
    * @param depth
    *   The search depth (number of plies).
    * @return
    *   The best move found, or None if no legal moves exist.
    */
  def bestMove(state: GameState, depth: Int): Option[Move] =
    val (_, bestMove) = minimax(state, depth, -Int.MaxValue, Int.MaxValue)
    bestMove

  /** Recursive Negamax function with Alpha-Beta pruning and Transposition Table
    * integration.
    *
    * This function uses a single match expression to handle all search states
    * and returns both the best score and the move that achieved it.
    *
    * @param state
    *   The current game state.
    * @param depth
    *   The remaining search depth.
    * @param alpha
    *   The lower bound of the score (floor).
    * @param beta
    *   The upper bound of the score (ceiling).
    * @return
    *   A tuple of (best score, best move).
    */
  def minimax(
      state: GameState,
      depth: Int,
      alpha: Int,
      beta: Int
  ): (Int, Option[Move]) =
    val alphaOriginal = alpha
    val cachedEntry = tt.lookup(state.hash)

    (cachedEntry, depth) match
      case (Some(entry), _)
          if entry.depth >= depth &&
            (
              entry.scoreType == Exact ||
                (entry.scoreType == LowerBound && entry.score >= beta) ||
                (entry.scoreType == UpperBound && entry.score <= alpha)
            ) =>
        (entry.score, Some(entry.bestMove))

      case (_, 0) =>
        // Leaf node: return static evaluation. No move is returned because
        // we didn't search any moves from this position.
        (Evaluation(state).score, None)

      case _ if isCheckmate(state) =>
        // Terminal node: checkmate. No move is possible.
        (-MateScore - depth, None)

      case _ if isStalemate(state) =>
        // Terminal node: stalemate. No move is possible.
        (StalemateScore, None)

      case _ =>
        /** Iterates through moves and returns the best score and move found. */
        def searchMove(
            moves: List[Move],
            bestScore: Int,
            bestMove: Option[Move]
        ): (Int, Option[Move]) =
          moves match
            case Nil       => (bestScore, bestMove)
            case m :: tail =>
              // Negamax recursion: negate the score returned by the child
              val (childScore, _) = minimax(
                state.applyMove(m),
                depth - 1,
                -beta,
                -bestScore
              )
              val score = -childScore

              if score >= beta then (score, Some(m))
              else
                val (nextBestScore, nextBestMove) =
                  if score > bestScore then (score, Some(m))
                  else (bestScore, bestMove)
                searchMove(tail, nextBestScore, nextBestMove)

        // Prioritize the move stored in the TT for better pruning efficiency.
        val ttMove: Option[Move] = cachedEntry.map(e => e.bestMove)
        val legalMoves = allLegalMoves(state).sortBy(m => -scoreMove(m, ttMove))
        val (bestScore, bestMove) = searchMove(legalMoves, alpha, None)

        val scoreType: ScoreType =
          if bestScore >= beta then LowerBound
          else if bestScore <= alphaOriginal then UpperBound
          else Exact

        bestMove.foreach(m =>
          tt.store(TTEntry(state.hash, depth, bestScore, scoreType, m))
        )

        (bestScore, bestMove)

  /** Assigns a priority score to a move to improve Alpha-Beta pruning
    * efficiency.
    *
    * Priority order:
    *   1. TT Move (highest)
    *   2. Captures (MVV-LVA)
    *   3. Normal moves (lowest)
    *
    * @param m
    *   The move to score.
    * @param ttMove
    *   The best move found in a previous search of this position.
    * @return
    *   An integer score representing the move's priority.
    */
  def scoreMove(m: Move, ttMove: Option[Move]): Int =
    if ttMove.contains(m) then 1000000
    else if m.capture.isDefined then 500000 + guessValue(m)
    else guessValue(m)

  /** Heuristic to guess the value of a move based on material gain. Uses
    * MVV-LVA (Most Valuable Victim - Least Valuable Attacker).
    */
  def guessValue(m: Move): Int = m match
    case NormalMove(_, _, _, Some(victim)) =>
      victim.role.weight * 10 - m.piece.role.weight
    case PromotionMove(_, _, _, promotion, Some(victim)) =>
      victim.role.weight * 10 + promotion.weight - m.piece.role.weight
    case PromotionMove(_, _, _, promotion, None) =>
      promotion.weight
    case _ => 0
