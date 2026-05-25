package chessengine.logic

import chessengine.TestHelpers.perft
import chessengine.domain.GameState

class MoveGeneratorSuite extends munit.FunSuite:

  test("initial position perft depth 1") {
    assertEquals(perft(GameState.initial, 1), 20)
  }

  test("initial position perft depth 2") {
    assertEquals(perft(GameState.initial, 2), 400)
  }
