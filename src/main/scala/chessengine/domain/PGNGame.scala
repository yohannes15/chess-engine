package chessengine.domain

final case class PGNGame(
    tags: Map[String, String],
    moves: List[Move]
)
