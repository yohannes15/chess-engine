package chessengine.api.dto

import chessengine.domain.Color
import io.circe.Encoder

final case class CheckMateResponse(winner: Color)

object CheckMateResponse:
  given checkmateResponseEncoder: Encoder[CheckMateResponse] =
    Encoder.forProduct1("winner")(_.winner.toString.toLowerCase)
