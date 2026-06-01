package chessengine.domain

final case class Piece(color: Color, role: Role):
  def toFenChar: Char =
    if color == Color.White then role.toSanChar else role.toSanChar.toLower
