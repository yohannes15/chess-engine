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
    def searchMove(
        moves: List[Move],
        bestSoFar: Option[Move],
        alpha: Int // AKA floor (min score that player is already assured of)
    ): Option[Move] =
      moves match
        case Nil       => bestSoFar
        case m :: tail =>
          val score = -minimax(
            state.applyMove(m),
            depth - 1,
            -Int.MaxValue,
            -alpha
          )
          val nextAlpha = Math.max(alpha, score)
          val nextMove = if score > alpha then Some(m) else bestSoFar
          searchMove(tail, nextMove, nextAlpha)

    val legalMoves = allLegalMoves(state).sortBy(m => -guessValue(m))
    searchMove(legalMoves, None, -Int.MaxValue)

  /** Recursive Negamax function with Alpha-Beta pruning. Returns the best
    * possible score for the player whose turn it is in the given state.
    *   - depth | "ply" - depth 0 is decision state
    *   - alpha | "floor" - minimum score curr player is already assured of
    *   - beta | "ceiling" - maximum score opponent is willing to allow
    */
  def minimax(state: GameState, depth: Int, alpha: Int, beta: Int): Int =
    if depth == 0 then
      Evaluation(state).score
    else if isCheckmate(state) then
      -MateScore - depth
    else if isStalemate(state) then
      StalemateScore
    else
      /** Iterates through moves and returns the best score found. If a score >=
        * beta is found, we "prune" (stop searching) because a smart opponent
        * would never let the game reach this position.
        */
      def searchMove(moves: List[Move], bestScoreSoFar: Int): Int =
        moves match
          case Nil       => bestScoreSoFar
          case m :: tail =>
            val score = -minimax(
              state.applyMove(m),
              depth - 1,
              -beta,
              -bestScoreSoFar
            )

            // Pruning (Fail-Soft)
            if score >= beta then
              // This move is so good for me that the opponent will veto this branch.
              // We return the score immediately and stop looking at other moves.
              score
            else
              // Update our best score and continue to the next move
              val nextBestScore = Math.max(bestScoreSoFar, score)
              searchMove(tail, nextBestScore)

      val legalMoves = allLegalMoves(state).sortBy(m => -guessValue(m))
      searchMove(legalMoves, alpha)

  def guessValue(m: Move): Int = m match
    case NormalMove(_, _, _, Some(victim)) =>
      victim.role.weight * 10 - m.piece.role.weight
    case PromotionMove(_, _, _, promotion, Some(victim)) =>
      victim.role.weight * 10 + promotion.weight - m.piece.role.weight
    case PromotionMove(_, _, _, promotion, None) =>
      promotion.weight
    case _ => 0
