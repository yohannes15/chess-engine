package chessengine.domain

import cats.data.Validated.{Invalid, Valid}
import chessengine.logic.MoveGenerator

class PGNSuite extends munit.FunSuite:

  // ---------------------------------------------------------------------------
  // Tag parsing
  // ---------------------------------------------------------------------------

  test("tags - multi-line PGN") {
    PGN.parse("""
      [Event "Casual Game"]
      [Date "2024.01.15"]
    """) match
      case Valid(g) =>
        assertEquals(g.tags("Event"), "Casual Game")
        assertEquals(g.tags("Date"), "2024.01.15")
      case _ => fail("expected Valid")
  }

  test("tags - no space before value") {
    PGN.parse("""[Event "Test"][Date"2024.01.15"]""") match
      case Valid(g) =>
        assertEquals(g.tags("Event"), "Test")
        assertEquals(g.tags("Date"), "2024.01.15")
      case _ => fail("expected Valid")
  }

  test("tags - empty value") {
    PGN.parse("""[Event ""]""") match
      case Valid(g) => assertEquals(g.tags("Event"), "")
      case _        => fail("expected Valid")
  }

  test("tags - invalid input is rejected") {
    assert(PGN.parse("garbage").isInvalid)
    assert(PGN.parse("").isInvalid)
  }

  // ---------------------------------------------------------------------------
  // Full game replay via parseMoves
  // ---------------------------------------------------------------------------

  test("parseMoves - fool's mate replays 4 moves") {
    // 1. f3 e5 2. g4 Qh4# — the fastest checkmate
    val pgn = """[Event "Test"] 1. f3 e5 2. g4 Qh4# 0-1"""
    PGN.parse(pgn) match
      case Valid(g) =>
        assertEquals(g.moves.length, 4)
        val finalState = g.moves.foldLeft(GameState.initial)(_.applyMove(_))
        assert(MoveGenerator.isCheckmate(finalState))
      case Invalid(e) => fail(s"expected Valid, got errors: $e")
  }

  test("parseMoves - empty move section yields Nil") {
    PGN.parse("""[Event "Test"]""") match
      case Valid(g) => assertEquals(g.moves, Nil)
      case _        => fail("expected Valid")
  }

  test("parseMoves - result token is stripped") {
    // result tokens must not become SAN tokens
    val pgn = """[Event "Test"] 1. e4 e5 1-0"""
    PGN.parse(pgn) match
      case Valid(g) => assertEquals(g.moves.length, 2)
      case _        => fail("expected Valid")
  }

  // ---------------------------------------------------------------------------
  // resolveSAN — piece moves
  // ---------------------------------------------------------------------------

  test("resolveSAN - pawn push e4") {
    val mv = PGN.resolveSAN("e4", GameState.initial).getOrElse(fail("failed"))
    assertEquals(mv.piece.role, Role.Pawn)
    assertEquals(mv.to, Square.fromNotation("e4").get)
  }

  test("resolveSAN - knight move Nf3") {
    val mv = PGN.resolveSAN("Nf3", GameState.initial).getOrElse(fail("failed"))
    assertEquals(mv.piece.role, Role.Knight)
    assertEquals(mv.to, Square.fromNotation("f3").get)
  }

  test("resolveSAN - check suffix is stripped before resolving") {
    // After 1.e4 e5 it is White's turn; Qh5 is legal (d1-e2-f3-g4-h5 diagonal is clear)
    val state = List("e4", "e5").foldLeft(GameState.initial) { (s, san) =>
      PGN.resolveSAN(
        san,
        s
      ).map(s.applyMove).getOrElse(fail(s"failed to apply $san"))
    }
    val withSuffix = PGN.resolveSAN("Qh5+", state)
    val withoutSuffix = PGN.resolveSAN("Qh5", state)
    assertEquals(withSuffix, withoutSuffix)
  }

  // ---------------------------------------------------------------------------
  // resolveSAN — castling
  // ---------------------------------------------------------------------------

  test("resolveSAN - kingside castling O-O") {
    // FEN: clear path between king and h-rook for both sides
    val fen = "r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w KQkq -"
    val state = Fen.parse(fen).getOrElse(fail("bad FEN"))
    val Right(move) = PGN.resolveSAN("O-O", state): @unchecked
    assert(move.isInstanceOf[CastlingMove])
    assertEquals(move.to.file, 6) // g-file
  }

  test("resolveSAN - queenside castling O-O-O") {
    val fen = "r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w KQkq -"
    val state = Fen.parse(fen).getOrElse(fail("bad FEN"))
    val Right(move) = PGN.resolveSAN("O-O-O", state): @unchecked
    assert(move.isInstanceOf[CastlingMove])
    assertEquals(move.to.file, 2) // c-file
  }

  // ---------------------------------------------------------------------------
  // resolveSAN — promotion
  // ---------------------------------------------------------------------------

  test("resolveSAN - pawn promotion a8=Q") {
    // White pawn on a7, kings far away
    val fen = "8/P7/8/8/8/8/8/K6k w - -"
    val state = Fen.parse(fen).getOrElse(fail("bad FEN"))
    val Right(move) = PGN.resolveSAN("a8=Q", state): @unchecked
    assert(move.isInstanceOf[PromotionMove])
    assertEquals(move.asInstanceOf[PromotionMove].promotion, Role.Queen)
  }

  test("resolveSAN - pawn promotion to knight a8=N") {
    val fen = "8/P7/8/8/8/8/8/K6k w - -"
    val state = Fen.parse(fen).getOrElse(fail("bad FEN"))
    val Right(move) = PGN.resolveSAN("a8=N", state): @unchecked
    assertEquals(move.asInstanceOf[PromotionMove].promotion, Role.Knight)
  }

  // ---------------------------------------------------------------------------
  // resolveSAN — disambiguation
  // ---------------------------------------------------------------------------

  test("resolveSAN - file disambiguation Raf1 vs Rhf1") {
    // Two rooks on a1 and h1, king on e3; both rooks can reach f1
    val fen = "8/8/8/8/8/4K3/8/R6R w - -"
    val state = Fen.parse(fen).getOrElse(fail("bad FEN"))
    val Right(raf1) = PGN.resolveSAN("Raf1", state): @unchecked
    val Right(rhf1) = PGN.resolveSAN("Rhf1", state): @unchecked
    assertEquals(raf1.from, Square.fromNotation("a1").get)
    assertEquals(rhf1.from, Square.fromNotation("h1").get)
    assertEquals(raf1.to, rhf1.to)
  }

  test("resolveSAN - ambiguous move without disambiguation returns Left") {
    // Both rooks can go to f1 but no disambiguation given
    val fen = "8/8/8/8/8/4K3/8/R6R w - -"
    val state = Fen.parse(fen).getOrElse(fail("bad FEN"))
    assert(PGN.resolveSAN("Rf1", state).isLeft)
  }

  test("resolveSAN - rank disambiguation N1f3 vs N5f3") {
    // Two knights on e1 and e5 both able to reach f3
    val fen = "8/8/8/4N3/8/8/8/4N2K w - -"
    val state = Fen.parse(fen).getOrElse(fail("bad FEN"))
    val Right(n1f3) = PGN.resolveSAN("N1f3", state): @unchecked
    val Right(n5f3) = PGN.resolveSAN("N5f3", state): @unchecked
    assertEquals(n1f3.from.rank, 0)
    assertEquals(n5f3.from.rank, 4)
  }

  // ---------------------------------------------------------------------------
  // resolveSAN — error cases
  // ---------------------------------------------------------------------------

  test("resolveSAN - illegal move returns Left") {
    // Pawn cannot jump 3 squares
    assert(PGN.resolveSAN("e5", GameState.initial).isLeft)
  }

  test("resolveSAN - invalid destination square returns Left") {
    assert(PGN.resolveSAN("Nz9", GameState.initial).isLeft)
  }

  // ---------------------------------------------------------------------------
  // toSAN encoder
  // ---------------------------------------------------------------------------

  test("toSAN - simple pawn push") {
    val Right(move) = PGN.resolveSAN("e4", GameState.initial): @unchecked
    assertEquals(PGN.toSAN(move, GameState.initial), "e4")
  }

  test("toSAN - knight move") {
    val Right(move) = PGN.resolveSAN("Nf3", GameState.initial): @unchecked
    assertEquals(PGN.toSAN(move, GameState.initial), "Nf3")
  }

  test("toSAN - appends # for checkmate (fool's mate last move)") {
    val stateBeforeQh4 = List("f3", "e5", "g4").foldLeft(GameState.initial) {
      (s, san) =>
        PGN.resolveSAN(san, s).map(s.applyMove).getOrElse(fail(s"failed: $san"))
    }
    val Right(qh4) = PGN.resolveSAN("Qh4", stateBeforeQh4): @unchecked
    assertEquals(PGN.toSAN(qh4, stateBeforeQh4), "Qh4#")
  }

  test("toSAN - castling kingside") {
    val fen = "r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w KQkq -"
    val state = Fen.parse(fen).getOrElse(fail("bad FEN"))
    val Right(move) = PGN.resolveSAN("O-O", state): @unchecked
    assertEquals(PGN.toSAN(move, state), "O-O")
  }

  test("toSAN - promotion") {
    val fen = "8/P7/8/8/8/8/8/K6k w - -"
    val state = Fen.parse(fen).getOrElse(fail("bad FEN"))
    val Right(move) = PGN.resolveSAN("a8=Q", state): @unchecked
    // The queen on a8 gives check to the black king on h1 along the diagonal
    assertEquals(PGN.toSAN(move, state), "a8=Q+")
  }

  test("toSAN - includes file disambiguation for two rooks") {
    val fen = "8/8/8/8/8/4K3/8/R6R w - -"
    val state = Fen.parse(fen).getOrElse(fail("bad FEN"))
    val Right(raf1) = PGN.resolveSAN("Raf1", state): @unchecked
    val Right(rhf1) = PGN.resolveSAN("Rhf1", state): @unchecked
    assertEquals(PGN.toSAN(raf1, state), "Raf1")
    assertEquals(PGN.toSAN(rhf1, state), "Rhf1")
  }

  test("toSAN round-trip - resolveSAN(toSAN(m, s), s) returns same move") {
    val Right(e4) = PGN.resolveSAN("e4", GameState.initial): @unchecked
    val san = PGN.toSAN(e4, GameState.initial)
    val Right(m2) = PGN.resolveSAN(san, GameState.initial): @unchecked
    assertEquals(m2.from, e4.from)
    assertEquals(m2.to, e4.to)
  }
