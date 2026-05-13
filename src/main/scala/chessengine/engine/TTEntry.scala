package chessengine.engine

import chessengine.domain.{Move}

final case class TTEntry(
    hash: Long,
    depth: Int,
    score: Int,
    scoreType: ScoreType,
    bestMove: Move
)
