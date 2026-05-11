package chessengine.domain

enum Color:
  case White, Black

  def opposite: Color = this match
    case Black => White
    case White => Black
