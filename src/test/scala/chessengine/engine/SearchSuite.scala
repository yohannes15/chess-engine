package chessengine.engine

import chessengine.domain.{
  Fen, GameState, NormalMove, Square, Piece, Color, Role
}
import SearchRes.*

class SearchSuite extends munit.FunSuite:
  test("checkmate detection - black is in checkmate") {
    val gs = Fen.parse(
      "7k/5KQ1/8/8/8/8/8/8 b - -"
    ).getOrElse(fail("unable to parse valid fen"))
    val search = Search(TranspositionTable(250))
    assertEquals(search.bestMove(gs, 0), CheckMate)
  }

  test("stalemate detection - black is in stalemate") {
    val gs = Fen.parse(
      "7k/5Q2/8/8/8/8/8/7K b - -"
    ).getOrElse(fail("unable to parse valid fen"))
    val search = Search(TranspositionTable(250))
    assertEquals(search.bestMove(gs, 0), StaleMate)
  }

  test("non-terminal position returns BestMove") {
    val gs = GameState.initial
    val search = Search(TranspositionTable(250))
    search.bestMove(gs, 1) match
      case BestMove(_, _) => ()
      case other          => fail(s"expected BestMove, got $other")
  }

  test("Qg7# delivers checkmate") {
    val gs = Fen.parse(
      "7k/8/5K2/8/8/8/8/6Q1 w - -"
    ).getOrElse(fail("unable to parse valid fen"))
    val afterQg7 = gs.applyMove(
      NormalMove(
        Square.fromNotation("g1").get,
        Square.fromNotation("g7").get,
        Piece(Color.White, Role.Queen),
        None
      )
    )
    assertEquals(Search(TranspositionTable(250)).bestMove(afterQg7, 0), CheckMate)
  }


