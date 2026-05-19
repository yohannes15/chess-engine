package chessengine.domain

enum GameOver:
  case CheckMate(winner: Color)
  case StaleMate

object GameOver:
  ???
