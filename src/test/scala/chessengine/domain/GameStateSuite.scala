package chessengine.domain

import chessengine.TestHelpers.startingHash

class GameStateSuite extends munit.FunSuite:
  test("initial creates the initial GameState") {
    val gs: GameState = GameState.initial
    assertEquals(gs.color, Color.White)
    assertEquals(gs.captures, Nil)
    assertEquals(gs.castlingRights, CastlingRights(true, true, true, true))
    assertEquals(gs.enPassantSquare, None)
    assertEquals(gs.board.pieces.flatten.length, 32)
    assertEquals(gs.hash, startingHash)
  }
