package chessengine.domain

import chessengine.TestHelpers.startingHash

class GameStateSuite extends munit.FunSuite:

  private def castlingFen(side: Char): String =
    s"r3k2r/8/8/8/8/8/8/R3K2R $side KQkq - 0 1"

  test("initial creates the initial GameState") {
    val gs = GameState.initial
    assertEquals(gs.color, Color.White)
    assertEquals(gs.captures, Nil)
    assertEquals(gs.castlingRights, CastlingRights(true, true, true, true))
    assertEquals(gs.enPassantSquare, None)
    assertEquals(gs.board.pieces.flatten.length, 32)
    assertEquals(gs.hash, startingHash)
  }

  test("applyMove with NormalMove flips active color") {
    val gs = GameState.initial
    val afterMove = gs.applyMove(
      NormalMove(
        Square.fromNotation("c2").get,
        Square.fromNotation("c4").get,
        Piece(Color.White, Role.Pawn),
        None
      )
    )
    assertEquals(afterMove.color, gs.color.opposite)
  }

  test("applyMove with double pawn push sets enPassantSquare") {
    val gs = GameState.initial
    val forward2Move = gs.applyMove(
      NormalMove(
        Square.fromNotation("c2").get,
        Square.fromNotation("c4").get,
        Piece(Color.White, Role.Pawn),
        None
      )
    )
    assert(forward2Move.enPassantSquare.isDefined)
  }

  test("applyMove with NormalMove clears enPassantSquare") {
    val gs = GameState.initial
    val forward2Move = gs.applyMove(
      NormalMove(
        Square.fromNotation("c2").get,
        Square.fromNotation("c4").get,
        Piece(Color.White, Role.Pawn),
        None
      )
    )
    assert(forward2Move.enPassantSquare.isDefined)
    val afterNormalMove = forward2Move.applyMove(
      NormalMove(
        Square.fromNotation("c7").get,
        Square.fromNotation("c6").get,
        Piece(Color.Black, Role.Pawn),
        None
      )
    )
    assert(!afterNormalMove.enPassantSquare.isDefined)
  }

  test("applyMove with capture appends captured piece to captures list") {
    val gs = Fen.parse( // pos where e4 can take d5
      "rnbqkbnr/ppp1pppp/8/3p4/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 2"
    )
    val afterCapture = gs.getOrElse(
      fail("Failed parsing valid FEN string")
    ).applyMove(
      NormalMove(
        Square.fromNotation("e4").get,
        Square.fromNotation("d5").get,
        Piece(Color.White, Role.Pawn),
        Some(Piece(Color.Black, Role.Pawn))
      )
    )

    assert(afterCapture.captures.contains(Piece(Color.Black, Role.Pawn)))
  }

  test("applyMove with no capture leaves captures list unchanged") {
    assert(
      GameState.initial.applyMove(
        NormalMove(
          Square.fromNotation("c2").get,
          Square.fromNotation("c4").get,
          Piece(Color.White, Role.Pawn),
          None
        )
      ).captures.isEmpty
    )
  }

  test("applyMove returns a new GameState without mutating the original") {
    val gs = GameState.initial
    val afterMove = gs.applyMove(
      NormalMove(
        Square.fromNotation("c2").get,
        Square.fromNotation("c4").get,
        Piece(Color.White, Role.Pawn),
        None
      )
    )
    assertNotEquals(afterMove.hash, gs.hash)
  }

  test("applyMove with White king move revokes both white castling rights") {
    val gs = Fen.parse(castlingFen('w')).getOrElse(
      fail("Invalid FEN")
    )
    val afterMove = gs.applyMove(
      NormalMove(
        Square.fromNotation("e1").get,
        Square.fromNotation("d2").get,
        Piece(Color.White, Role.King),
        None
      )
    )
    assert(!afterMove.castlingRights.whiteKingSide)
    assert(!afterMove.castlingRights.whiteQueenSide)
    // black rights preserved
    assert(afterMove.castlingRights.blackKingSide)
    assert(afterMove.castlingRights.blackQueenSide)
  }

  test("applyMove with Black king move revokes both black castling rights") {
    val gs = Fen.parse(castlingFen('b')).getOrElse(
      fail("Invalid FEN")
    )
    val afterMove = gs.applyMove(
      NormalMove(
        Square.fromNotation("e8").get,
        Square.fromNotation("d8").get,
        Piece(Color.Black, Role.King),
        None
      )
    )
    assert(!afterMove.castlingRights.blackKingSide)
    assert(!afterMove.castlingRights.blackQueenSide)
    // white castling rights preserved
    assert(afterMove.castlingRights.whiteKingSide)
    assert(afterMove.castlingRights.whiteQueenSide)
  }

  test("applyMove with White rook from a1 revokes white queen-side castling") {
    val gs = Fen.parse(castlingFen('w')).getOrElse(
      fail("Invalid FEN")
    )
    val afterMove = gs.applyMove(
      NormalMove(
        Square.fromNotation("a1").get,
        Square.fromNotation("a8").get,
        Piece(Color.White, Role.Rook),
        Some(Piece(Color.Black, Role.Rook))
      )
    )
    assert(!afterMove.castlingRights.whiteQueenSide)
    assert(!afterMove.castlingRights.blackQueenSide) // rook captured on a8
    // other rights preserved
    assert(afterMove.castlingRights.whiteKingSide)
    assert(afterMove.castlingRights.blackKingSide)
  }

  test("applyMove with White rook from h1 revokes white king-side castling") {
    val gs = Fen.parse(castlingFen('w')).getOrElse(
      fail("Invalid FEN")
    )
    val afterMove = gs.applyMove(
      NormalMove(
        Square.fromNotation("h1").get,
        Square.fromNotation("h8").get,
        Piece(Color.White, Role.Rook),
        Some(Piece(Color.Black, Role.Rook))
      )
    )
    assert(!afterMove.castlingRights.whiteKingSide)
    assert(!afterMove.castlingRights.blackKingSide) // rook captured on h8
    // other rights preserved
    assert(afterMove.castlingRights.whiteQueenSide)
    assert(afterMove.castlingRights.blackQueenSide)
  }

  test("applyMove with Black rook from a8 revokes black queen-side castling") {
    val gs = Fen.parse(castlingFen('b')).getOrElse(
      fail("Invalid FEN")
    )
    val afterMove = gs.applyMove(
      NormalMove(
        Square.fromNotation("a8").get,
        Square.fromNotation("a1").get,
        Piece(Color.Black, Role.Rook),
        Some(Piece(Color.White, Role.Rook))
      )
    )
    assert(!afterMove.castlingRights.blackQueenSide)
    assert(!afterMove.castlingRights.whiteQueenSide) // rook captured on a1
    // other rights preserved
    assert(afterMove.castlingRights.blackKingSide)
    assert(afterMove.castlingRights.whiteKingSide)
  }

  test("applyMove with Black rook from h8 revokes black king-side castling") {
    val gs = Fen.parse(castlingFen('b')).getOrElse(
      fail("Invalid FEN")
    )
    val afterMove = gs.applyMove(
      NormalMove(
        Square.fromNotation("h8").get,
        Square.fromNotation("h1").get,
        Piece(Color.Black, Role.Rook),
        Some(Piece(Color.White, Role.Rook))
      )
    )
    assert(!afterMove.castlingRights.blackKingSide)
    assert(!afterMove.castlingRights.whiteKingSide) // rook captured on h1
    // other rights preserved
    assert(afterMove.castlingRights.blackQueenSide)
    assert(afterMove.castlingRights.whiteQueenSide)
  }

  test("applyMove capturing rook on a1 revokes white queen-side castling") {
    val gs = Fen.parse("4k3/8/8/8/8/8/1n6/R3K2R b KQkq - 0 1").getOrElse(
      fail("Invalid FEN")
    )
    val afterMove = gs.applyMove(
      NormalMove(
        Square.fromNotation("b3").get,
        Square.fromNotation("a1").get,
        Piece(Color.Black, Role.Knight),
        Some(Piece(Color.White, Role.Rook))
      )
    )
    assert(!afterMove.castlingRights.whiteQueenSide)
    // other rights preserved
    assert(afterMove.castlingRights.whiteKingSide)
    assert(afterMove.castlingRights.blackKingSide)
    assert(afterMove.castlingRights.blackQueenSide)
  }

  test("applyMove capturing rook on h1 revokes white king-side castling") {
    val gs = Fen.parse("4k3/8/8/8/8/8/6n1/R3K2R b KQkq - 0 1").getOrElse(
      fail("Invalid FEN")
    )
    val afterMove = gs.applyMove(
      NormalMove(
        Square.fromNotation("g3").get,
        Square.fromNotation("h1").get,
        Piece(Color.Black, Role.Knight),
        Some(Piece(Color.White, Role.Rook))
      )
    )
    assert(!afterMove.castlingRights.whiteKingSide)
    // other rights preserved
    assert(afterMove.castlingRights.whiteQueenSide)
    assert(afterMove.castlingRights.blackKingSide)
    assert(afterMove.castlingRights.blackQueenSide)
  }

  test("applyMove capturing rook on a8 revokes black queen-side castling") {
    val gs = Fen.parse("r3k2r/8/8/8/8/8/1N6/4K3 w KQkq - 0 1").getOrElse(
      fail("Invalid FEN")
    )
    val afterMove = gs.applyMove(
      NormalMove(
        Square.fromNotation("b3").get,
        Square.fromNotation("a8").get,
        Piece(Color.White, Role.Knight),
        Some(Piece(Color.Black, Role.Rook))
      )
    )
    assert(!afterMove.castlingRights.blackQueenSide)
    // other rights preserved
    assert(afterMove.castlingRights.blackKingSide)
    assert(afterMove.castlingRights.whiteKingSide)
    assert(afterMove.castlingRights.whiteQueenSide)
  }

  test("applyMove capturing rook on h8 revokes black king-side castling") {
    val gs = Fen.parse("r3k2r/8/8/8/8/8/6N1/4K3 w KQkq - 0 1").getOrElse(
      fail("Invalid FEN")
    )
    val afterMove = gs.applyMove(
      NormalMove(
        Square.fromNotation("g3").get,
        Square.fromNotation("h8").get,
        Piece(Color.White, Role.Knight),
        Some(Piece(Color.Black, Role.Rook))
      )
    )
    assert(!afterMove.castlingRights.blackKingSide)
    // other rights preserved
    assert(afterMove.castlingRights.blackQueenSide)
    assert(afterMove.castlingRights.whiteKingSide)
    assert(afterMove.castlingRights.whiteQueenSide)
  }

  test("applyMove with non-king non-rook move preserves all castling rights") {
    val gs = GameState.initial
    val afterMove = gs.applyMove(
      NormalMove(
        Square.fromNotation("c2").get,
        Square.fromNotation("c4").get,
        Piece(Color.White, Role.Pawn),
        None
      )
    )
    assert(afterMove.castlingRights.whiteKingSide)
    assert(afterMove.castlingRights.whiteQueenSide)
    assert(afterMove.castlingRights.blackKingSide)
    assert(afterMove.castlingRights.blackQueenSide)
  }

  test("applyMove with CastlingMove moves king and rook") {
    val gs = Fen.parse(castlingFen('w')).getOrElse(
      fail("Invalid FEN")
    )
    val afterMove = gs.applyMove(
      CastlingMove(
        Square.fromNotation("e1").get,
        Square.fromNotation("g1").get,
        Square.fromNotation("h1").get,
        Square.fromNotation("f1").get,
        Piece(Color.White, Role.King),
        None
      )
    )
    assertEquals(
      afterMove.board.pieces(Square.fromNotation("g1").get.index),
      Some(Piece(Color.White, Role.King))
    )
    assertEquals(
      afterMove.board.pieces(Square.fromNotation("f1").get.index),
      Some(Piece(Color.White, Role.Rook))
    )
    assertEquals(
      afterMove.board.pieces(Square.fromNotation("e1").get.index),
      None
    )
    assertEquals(
      afterMove.board.pieces(Square.fromNotation("h1").get.index),
      None
    )
  }

  test(
    "applyMove with CastlingMove revokes both castling rights for that color"
  ) {
    val gs = Fen.parse(castlingFen('w')).getOrElse(
      fail("Invalid FEN")
    )
    val afterMove = gs.applyMove(
      CastlingMove(
        Square.fromNotation("e1").get,
        Square.fromNotation("g1").get,
        Square.fromNotation("h1").get,
        Square.fromNotation("f1").get,
        Piece(Color.White, Role.King),
        None
      )
    )
    assert(!afterMove.castlingRights.whiteKingSide)
    assert(!afterMove.castlingRights.whiteQueenSide)
    // black rights preserved
    assert(afterMove.castlingRights.blackKingSide)
    assert(afterMove.castlingRights.blackQueenSide)
  }

  test("applyMove with PromotionMove promotes pawn to chosen role") {
    val gs = Fen.parse("4k3/P7/8/8/8/8/8/R3K2R w - - 0 1").getOrElse(
      fail("Invalid FEN")
    )
    val afterMove = gs.applyMove(
      PromotionMove(
        Square.fromNotation("a7").get,
        Square.fromNotation("a8").get,
        Piece(Color.White, Role.Pawn),
        Role.Queen,
        None
      )
    )
    assertEquals(
      afterMove.board.pieces(Square.fromNotation("a8").get.index),
      Some(Piece(Color.White, Role.Queen))
    )
    assertEquals(
      afterMove.board.pieces(Square.fromNotation("a7").get.index),
      None
    )
    assert(afterMove.captures.isEmpty)
  }

  test("applyMove with PromotionMove with capture adds captured piece") {
    val gs = Fen.parse("r1k5/P7/8/8/8/8/8/R3K2R w - - 0 1").getOrElse(
      fail("Invalid FEN")
    )
    val afterMove = gs.applyMove(
      PromotionMove(
        Square.fromNotation("a7").get,
        Square.fromNotation("a8").get,
        Piece(Color.White, Role.Pawn),
        Role.Queen,
        Some(Piece(Color.Black, Role.Rook))
      )
    )
    assertEquals(
      afterMove.board.pieces(Square.fromNotation("a8").get.index),
      Some(Piece(Color.White, Role.Queen))
    )
    assertEquals(
      afterMove.board.pieces(Square.fromNotation("a7").get.index),
      None
    )
    assert(afterMove.captures.contains(Piece(Color.Black, Role.Rook)))
  }

  test("applyMove with EnPassantMove removes the captured pawn") {
    val gs = Fen.parse(
      "rnbqkbnr/ppp1pppp/8/3pP3/8/8/PPPP1PPP/RNBQKBNR w KQkq d6 0 3"
    ).getOrElse(
      fail("Invalid FEN")
    )
    val afterMove = gs.applyMove(
      EnPassantMove(
        Square.fromNotation("e5").get,
        Square.fromNotation("d6").get,
        Piece(Color.White, Role.Pawn),
        Some(Piece(Color.Black, Role.Pawn))
      )
    )
    // white pawn now on d6
    assertEquals(
      afterMove.board.pieces(Square.fromNotation("d6").get.index),
      Some(Piece(Color.White, Role.Pawn))
    )
    // original square empty
    assertEquals(
      afterMove.board.pieces(Square.fromNotation("e5").get.index),
      None
    )
    // captured pawn removed from d5
    assertEquals(
      afterMove.board.pieces(Square.fromNotation("d5").get.index),
      None
    )
    assert(afterMove.captures.contains(Piece(Color.Black, Role.Pawn)))
  }
