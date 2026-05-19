package chessengine.api.dto

import io.circe.Encoder

final case class StalemateResponse()

object StalemateResponse:
  given stalemateResponseEncoder: Encoder[StalemateResponse] =
    Encoder.forProduct0(_ => ())
