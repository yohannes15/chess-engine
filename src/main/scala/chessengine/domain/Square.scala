package chessengine.domain

opaque type Square = Int
object Square:
  def fromInt(i: Int): Option[Square] =
    if i >= 0 && i < 64 then Some(i) else None
