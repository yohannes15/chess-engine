package chessengine.domain

import cats.data.Validated
import cats.data.Validated.Valid
import chessengine.TestHelpers.startingFen

class FenSuite extends munit.FunSuite:

  test("empty string fails") { assert(Fen.parse("").isInvalid) }

  test("wrong number of fields fails") {
    assert(Fen.parse("one two").isInvalid)
    assert(Fen.parse("one two three four five").isInvalid)
    assert(Fen.parse("one two three four five six seven").isInvalid)
  }

  test("wrong number of ranks fails") {
    assert(
      Fen.parse("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP w KQkq - 0 1").isInvalid
    )
  }

  test("invalid piece character fails") {
    assert(Fen.parse(
      "xnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
    ).isInvalid)
  }

  test("invalid active color fails") {
    assert(Fen.parse(
      "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR x KQkq - 0 1"
    ).isInvalid)
  }

  test("invalid castling character fails") {
    assert(Fen.parse(
      "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w Kxkg - 0 1"
    ).isInvalid)
  }

  test("duplicate castling rights fail") {
    assert(Fen.parse(
      "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KKqk - 0 1"
    ).isInvalid)
  }

  test("invalid en passant square fails") {
    assert(Fen.parse(
      "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq e9 0 1"
    ).isInvalid)
  }

  test("starting position parses successfully") {
    assert(Fen.parse(startingFen).isValid)
  }

  test("starting position has white to move") {
    val result = Fen.parse(startingFen).map(_.color)
    assertEquals(result, Valid(Color.White))
  }

  test("starting position has correct piece count") {
    val result = Fen.parse(startingFen).map(_.board.pieces.flatten.size)
    assertEquals(result, Valid(32))
  }

  test("starting position has all castling rights") {
    val result = Fen.parse(startingFen).map(_.castlingRights)
    assertEquals(result, Valid(CastlingRights(true, true, true, true)))
  }

  test("FEN with no castling rights parses") {
    val result =
      Fen.parse("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w - - 0 1")
    assert(result.isValid)
    result.map { state =>
      assertEquals(
        state.castlingRights,
        CastlingRights(false, false, false, false)
      )
    }
  }

  test("FEN with black to move") {
    val result =
      Fen.parse("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1")
    assertEquals(result.map(_.color), Validated.Valid(Color.Black))
  }

  test("FEN with en passant square") {
    val result =
      Fen.parse("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1")
    assert(result.isValid)
    result.map { state =>
      assert(state.enPassantSquare.isDefined)
      assertEquals(state.enPassantSquare.map(_.toNotation), Some("e3"))
    }
  }

  test("4-field FEN (without move counters) parses") {
    assert(
      Fen.parse("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq -").isValid
    )
  }

  private def roundTrips(fen: String): Unit =
    assertEquals(Fen.parse(fen).map(Fen.write), Valid(fen))

  test("write starting position matches starting FEN") {
    roundTrips(startingFen)
  }

  test("write no-castling position round-trips") {
    roundTrips("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w - - 0 1")
  }

  test("write en-passant + black-to-move round-trips") {
    roundTrips("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1")
  }

  test("write empty board round-trips") {
    roundTrips("8/8/8/8/8/8/8/8 w - - 0 1")
  }

  test("write position with scattered pieces round-trips") {
    roundTrips("r3k2r/pp6/8/8/8/8/PP6/R3K2R w KQkq - 0 1")
  }

  test("write single-piece positions round-trips") {
    roundTrips("8/8/8/4k3/8/8/8/8 w - - 0 1")
  }

  test("write 4-field parsed FEN includes default move counters") {
    val fourField = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq -"
    assertEquals(
      Fen.parse(fourField).map(Fen.write),
      Valid("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
    )
  }
