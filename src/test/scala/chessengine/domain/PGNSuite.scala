package chessengine.domain

import cats.data.Validated.Valid

class PGNSuite extends munit.FunSuite:
  test("parse tags - multi-line") {
    PGN.parse("""
      [Event "Casual Game"]
      [Date "2024.01.15"]
    """) match
      case Valid(a) =>
        assertEquals(a("Event"), "Casual Game")
        assertEquals(a("Date"), "2024.01.15")
      case _ => fail("failed on multi-line tags")
  }

  test("parse tags - no space before value allowed") {
    // no space before value
    PGN.parse("""[Event "Test"][Date"2024.01.15"]""") match
      case Valid(a) =>
        assertEquals(a("Event"), "Test")
        assertEquals(a("Date"), "2024.01.15")
      case _ => fail("failed on no-space format")
  }

  test("parse tags - empty value allowed") {
    // empty value
    PGN.parse("""[Event ""]""") match
      case Valid(a) => assertEquals(a("Event"), "")
      case _        => fail("failed on empty value")
  }

  test("parse tags - invalid tag strings") {
    assert(PGN.parse("garbage").isInvalid)
    assert(PGN.parse("").isInvalid)
  }
