package chessengine.domain

opaque type Square = Int
object Square:
  def fromInt(i: Int): Option[Square] =
    if i >= 0 && i < 64 then Some(i) else None

  def fromRankAndFile(rank: Int, file: Int): Option[Square] =
    // How would you turn a rank (0-7) and file (0-7) back into a 0-63 index?
    if (rank >= 0 && rank < 8) && (file >= 0 && file < 8) then
      Some(rank * 8 + file)
    else
      None

  def fromNotation(s: String): Option[Square] =
    if s.size != 2 || !s(0).isLetter || !s(1).isDigit then
      None
    else
      val file = s(0).toLower - 'a'
      val rank = s(1) - '1'
      fromRankAndFile(rank, file)

extension (s: Square)
  def index: Int = s // underlying Int
  def rank: Int = s / 8
  def file: Int = s % 8
  def flip: Int = s ^ 56 // vertical flipping -> (7 - rank) * + file
  def toNotation: String =
    // 0 -> a1, 1 -> a2 ...
    val file = ('a' + s.file).toChar
    val rank = ('1' + s.rank).toChar
    s"$file$rank"
