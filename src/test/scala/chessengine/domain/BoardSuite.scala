package chessengine.domain

class BoardSuite extends munit.FunSuite:
  test("empty creates empty board") {
    val board = Board.empty
    assert(board.pieces.forall(_.isEmpty))
  }

  test("update places a piece on the board") {
    val board = Board.empty.update(
      Square.fromInt(0).get,
      Some(Piece(Color.White, Role.Rook))
    )
    assertEquals(board.pieces(0), Some(Piece(Color.White, Role.Rook)))
  }

  test("update removes a piece from the board") {
    val board = Board.empty
      .update(Square.fromInt(0).get, Some(Piece(Color.White, Role.Rook)))
      .update(Square.fromInt(0).get, None)
    assertEquals(board.pieces(0), None)
  }

  test("isEmptyAt returns true for empty square") {
    assert(Board.empty.isEmptyAt(Square.fromInt(0).get))
  }

  test("isEmptyAt returns false for occupied square") {
    val board = Board.empty.update(
      Square.fromInt(0).get,
      Some(Piece(Color.White, Role.Pawn))
    )
    assert(!board.isEmptyAt(Square.fromInt(0).get))
  }

  test("findPiece locates white king on initial board") {
    val square = Board.initial.findPiece(Color.White, Role.King)
    assertEquals(square.map(_.index), Some(4))
  }

  test("findPiece locates black king on initial board") {
    val square = Board.initial.findPiece(Color.Black, Role.King)
    assertEquals(square.map(_.index), Some(60))
  }

  test("findPiece returns None for a piece not on the board") {
    assertEquals(Board.empty.findPiece(Color.White, Role.King), None)
  }

  test("initial creates the starting chess position") {
    val board = Board.initial
    assertEquals(board.pieces.length, 64)
    assertEquals(board.pieces.flatten.length, 32)

    assertEquals(board.pieces(0), Some(Piece(Color.White, Role.Rook)))
    assertEquals(board.pieces(1), Some(Piece(Color.White, Role.Knight)))
    assertEquals(board.pieces(2), Some(Piece(Color.White, Role.Bishop)))
    assertEquals(board.pieces(3), Some(Piece(Color.White, Role.Queen)))
    assertEquals(board.pieces(4), Some(Piece(Color.White, Role.King)))
    assertEquals(board.pieces(5), Some(Piece(Color.White, Role.Bishop)))
    assertEquals(board.pieces(6), Some(Piece(Color.White, Role.Knight)))
    assertEquals(board.pieces(7), Some(Piece(Color.White, Role.Rook)))

    (8 to 15).foreach(i =>
      assertEquals(board.pieces(i), Some(Piece(Color.White, Role.Pawn)))
    )

    (16 to 47).foreach(i => assertEquals(board.pieces(i), None))

    (48 to 55).foreach(i =>
      assertEquals(board.pieces(i), Some(Piece(Color.Black, Role.Pawn)))
    )

    assertEquals(board.pieces(56), Some(Piece(Color.Black, Role.Rook)))
    assertEquals(board.pieces(57), Some(Piece(Color.Black, Role.Knight)))
    assertEquals(board.pieces(58), Some(Piece(Color.Black, Role.Bishop)))
    assertEquals(board.pieces(59), Some(Piece(Color.Black, Role.Queen)))
    assertEquals(board.pieces(60), Some(Piece(Color.Black, Role.King)))
    assertEquals(board.pieces(61), Some(Piece(Color.Black, Role.Bishop)))
    assertEquals(board.pieces(62), Some(Piece(Color.Black, Role.Knight)))
    assertEquals(board.pieces(63), Some(Piece(Color.Black, Role.Rook)))
  }
