package chessengine.engine

import chessengine.domain.*
import chessengine.logic.MoveGenerator.{allLegalMoves, isCheckmate, isStalemate}

object Search:

  private val MateScore = 1000000
  private val StalemateScore = 0

  /** Top-level function to find the best move for the current player.
    *
    * NOTE: what is good for the opposite player is bad for the current player
    * and what is bad for the opposite player is good for the current player
    * hence then -minimax(...) part. We use maxBy to find the move that results
    * in the highest Negamax score. The score of a move is the negative of the
    * opponent's best score. Example below using depth = 3
    *
    * ┌─────────┬──────────────────┬────────────┬─────────────────────────────────────────┐
    * │ Level   │ Who is thinking? │ Their Goal │ The "Handshake"                         │
    * ├─────────┼──────────────────┼────────────┼─────────────────────────────────────────┤
    * │ Root    │ White            │ Maximize   │ "I'll take the best of -Black's result" │
    * | Depth 2 │ Black            │ Maximize   │ "I'll take the best of -White's result" │
    * | Depth 1 │ White            │ Maximize   │ "I'll take the best of -Black's result" │
    * | Depth 0 │ (None)           │ Evaluate   │ "I'm just a scoreboard."                |
    * └─────────┴──────────────────┴────────────┴─────────────────────────────────────────┘
    *
    * Every time you go down one level in the tree, you flip the board.
    * The negative sign -minimax is the mathematical way of saying: "Your gain is my loss."
    */
  def bestMove(state: GameState, depth: Int = 3): Option[Move] =
    val moves = allLegalMoves(state)
    moves match
      case Nil => None
      case _   => Some(
          moves.maxBy(m =>
            val nextTurnBestScore = minimax(state.applyMove(m), depth - 1)
            // what is good for my opponent is bad for current and vice versa
            -nextTurnBestScore
          )
        )

  /** Recursive Negamax function. Returns the best possible score for the player
    * whose turn it is in the given state.
    *
    * NOTE: Answers the question I'm about to move from this state. If I play
    * perfectly for the next `depth` steps, whats the best score I can get. The
    * -minimax(...) part is explained above in bestMove, same reasoning here.
    */
  def minimax(state: GameState, depth: Int): Int =
    if depth == 0 then
      Evaluation(state).score
    else if isCheckmate(state) then
      -MateScore - depth
    else if isStalemate(state) then
      StalemateScore
    else
      // Standard Negamax recursion: max of -(opponent's best score)
      allLegalMoves(state).map(m =>
        val nextTurnBestScore = minimax(state.applyMove(m), depth - 1)
        // what is good for my opponent is bad for current and vice versa
        -nextTurnBestScore
      ).max
