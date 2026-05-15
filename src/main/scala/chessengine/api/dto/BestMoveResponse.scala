package chessengine.api.dto

import chessengine.domain.Move
import io.circe.Encoder

final case class BestMoveResponse(move: Move, score: Int)

object BestMoveResponse:
  given bestMoveResponseEncoder: Encoder[BestMoveResponse] =
    Encoder.forProduct2("move", "score")(bmr => (bmr.move.toUCI, bmr.score))
