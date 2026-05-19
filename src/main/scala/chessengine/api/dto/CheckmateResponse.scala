package chessengine.api.dto

import chessengine.domain.Color
import io.circe.Encoder

final case class CheckmateResponse(winner: Color)

object CheckmateResponse:
  given checkmateResponseEncoder: Encoder[CheckmateResponse] =
    Encoder.forProduct1("winner")(_.winner.toString.toLowerCase)
