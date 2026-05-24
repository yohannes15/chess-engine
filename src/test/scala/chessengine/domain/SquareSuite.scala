package chessengine.domain

class SquareSuite extends munit.FunSuite:
  test("square with index below 0 isn't possible") {
    val sq: Option[Square] = Square.fromInt(-1)
    assertEquals(sq, None)
  }
  test("square with index greater than 63 isn't possible") {
    val sq: Option[Square] = Square.fromInt(64)
    assertEquals(sq, None)
  }
  (0 to 63).foreach(x =>
    test(s"square with index $x is allowed") {
      assertEquals(Square.fromInt(x).map(_.index), Some(x))
    }
  )
  test("square with rank below 0 isn't possible") {
    val sq: Option[Square] = Square.fromRankAndFile(-1, 2)
    assertEquals(sq, None)
  }
  test("square with rank greater than 7 isn't possible") {
    val sq: Option[Square] = Square.fromRankAndFile(8, 2)
    assertEquals(sq, None)
  }
  test("square with file below 0 isn't possible") {
    val sq: Option[Square] = Square.fromRankAndFile(2, -1)
    assertEquals(sq, None)
  }
  test("square with file greater than 7 isn't possible") {
    val sq: Option[Square] = Square.fromRankAndFile(2, 8)
    assertEquals(sq, None)
  }
  (0 to 7).foreach(r =>
    (0 to 7).foreach(f =>
      test(s"square with rank $r and file $f is allowed") {
        assertEquals(
          Square.fromRankAndFile(r, f).map(_.index.isValidInt),
          Some(true)
        )
      }
    )
  )
  test("fromNotation with invalid string returns None") {
    assertEquals(Square.fromNotation("e5 "), None)
    assertEquals(Square.fromNotation(""), None)
    assertEquals(Square.fromNotation("e"), None)
    assertEquals(Square.fromNotation("e55"), None)
    assertEquals(Square.fromNotation("5e"), None)
  }
  test("fromNotation returns correct index for a1") {
    assertEquals(Square.fromNotation("a1").map(_.index), Some(0))
  }
  test("fromNotation returns correct index for h8") {
    assertEquals(Square.fromNotation("h8").map(_.index), Some(63))
  }
  test("fromNotation returns correct index for e4") {
    assertEquals(Square.fromNotation("e4").map(_.index), Some(28))
  }
  test("fromNotation accepts uppercase letters") {
    assertEquals(
      Square.fromNotation("A1").map(_.index),
      Square.fromNotation("a1").map(_.index)
    )
  }
