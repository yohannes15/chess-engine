package chessengine.engine

import chessengine.domain.{Color, Move}

enum SearchResult:
  case CheckMate(winner: Color)
  case StaleMate
  case BestMove(move: Move, score: Int)
