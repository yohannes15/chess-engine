package chessengine.engine

import chessengine.domain.{Fen, GameState, NormalMove, Square, Piece, Color, Role}
import chessengine.TestHelpers.startingFen

class EvaluationSuite extends munit.FunSuite:
  test("initial position is equal") {
    val gs = Fen.parse(startingFen).getOrElse(fail("unable to parse valid fen"))
    val eval = Evaluation(gs)
    assertEquals(eval.score, 0)
    assertEquals(eval.materialBalance, 0)
    assertEquals(eval.positionalBalance, 0)
  }

  test("white is up a pawn (black missing e7)") {
    val gs = Fen.parse(
      "rnbqkbnr/pppp1ppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
    ).getOrElse(fail("unable to parse valid fen"))
    val eval = Evaluation(gs)
    assertEquals(eval.score, 80)
    assertEquals(eval.materialBalance, 100)
    assertEquals(eval.positionalBalance, -20)
  }

  test("white is up a knight (black missing b8)") {
    val gs = Fen.parse(
      "r1bqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
    ).getOrElse(fail("unable to parse valid fen"))
    val eval = Evaluation(gs)
    assertEquals(eval.score, 280)
    assertEquals(eval.materialBalance, 320)
    assertEquals(eval.positionalBalance, -40)
  }

  test("knight g1 to f3 improves positional score by 50") {
    val nf3 = NormalMove(
      Square.fromNotation("g1").get,
      Square.fromNotation("f3").get,
      Piece(Color.White, Role.Knight),
      None
    )
    val gs = GameState.initial.applyMove(nf3)
    val eval = Evaluation(gs)
    // After Nf3 it's Black's turn. Score is from Black's perspective:
    // White has +50 positional advantage, so Black sees -50.
    assertEquals(eval.score, -50)
    assertEquals(eval.materialBalance, 0)
    assertEquals(eval.positionalBalance, 50)
  }

  test("score flips sign for black to move") {
    val gs = Fen.parse(
      "rnbqkbnr/pppp1ppp/8/8/8/8/PPPPPPPP/RNBQKBNR b KQkq - 0 1"
    ).getOrElse(fail("unable to parse valid fen"))
    val eval = Evaluation(gs)
    assertEquals(eval.score, -80)
    assertEquals(eval.materialBalance, 100)
    assertEquals(eval.positionalBalance, -20)
  }

  test("black queen d8 to d6") {
    val gs = Fen.parse(
      "rnb1kbnr/pppppppp/3q4/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
    ).getOrElse(fail("unable to parse valid fen"))
    val eval = Evaluation(gs)
    assertEquals(eval.score, -10)
    assertEquals(eval.materialBalance, 0)
    assertEquals(eval.positionalBalance, -10)
  }
