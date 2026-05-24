package chessengine.engine

import chessengine.domain.Move

enum SearchRes:
  case CheckMate, StaleMate
  case BestMove(move: Move, score: Int)
