package chessengine.logic

import chessengine.domain.{GameState, Fen}
import chessengine.TestHelpers.*

class MoveGeneratorSuite extends munit.FunSuite:

  test("initial perft depth 1") {
    assertEquals(perft(GameState.initial, 1), 20)
  }

  test("initial perft depth 2") {
    assertEquals(perft(GameState.initial, 2), 400)
  }

  test("initial perft depth 3") {
    assertEquals(perft(GameState.initial, 3), 8902)
  }

  test("initial perft depth 4") {
    assertEquals(perft(GameState.initial, 4), 197281)
  }

  test("KiwiPete perft depth 1") {
    val gs = Fen.parse(kiwiPeteFen).getOrElse(fail("failed to parse fen"))
    assertEquals(perft(gs, 1), 48)
  }

  test("KiwiPete perft depth 2") {
    val gs = Fen.parse(kiwiPeteFen).getOrElse(fail("failed to parse fen"))
    assertEquals(perft(gs, 2), 2039)
  }

  test("KiwiPete perft depth 3") {
    val gs = Fen.parse(kiwiPeteFen).getOrElse(fail("failed to parse fen"))
    assertEquals(perft(gs, 3), 97862)
  }

  test("Position 3 perft depth 1") {
    val gs =
      Fen.parse(position3Fen).getOrElse(fail("failed to parse fen"))
    assertEquals(perft(gs, 1), 14)
  }

  test("Position 3 perft depth 2") {
    val gs =
      Fen.parse(position3Fen).getOrElse(fail("failed to parse fen"))
    assertEquals(perft(gs, 2), 191)
  }

  test("Position 3 perft depth 3") {
    val gs =
      Fen.parse(position3Fen).getOrElse(fail("failed to parse fen"))
    assertEquals(perft(gs, 3), 2812)
  }

  test("Position 3 perft depth 4") {
    val gs =
      Fen.parse(position3Fen).getOrElse(fail("failed to parse fen"))
    assertEquals(perft(gs, 4), 43238)
  }

  test("Position 4 perft depth 1") {
    val gs = Fen.parse(position4Fen).getOrElse(fail("failed to parse fen"))
    assertEquals(perft(gs, 1), 6)
  }

  test("Position 4 perft depth 2") {
    val gs = Fen.parse(position4Fen).getOrElse(fail("failed to parse fen"))
    assertEquals(perft(gs, 2), 264)
  }

  test("Position 4 perft depth 3") {
    val gs = Fen.parse(position4Fen).getOrElse(fail("failed to parse fen"))
    assertEquals(perft(gs, 3), 9467)
  }
