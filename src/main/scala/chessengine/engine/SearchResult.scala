package chessengine.engine

import chessengine.domain.{Color, Move}

enum SearchResult:
  case CheckMate, StaleMate
  case BestMove(move: Move, score: Int)
