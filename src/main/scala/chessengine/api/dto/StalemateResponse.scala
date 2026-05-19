package chessengine.api.dto

import io.circe.Encoder
import io.circe.Json

final case class StalemateResponse()

object StalemateResponse:
  given stalemateResponseEncoder: Encoder[StalemateResponse] =
    Encoder.instance(_ => Json.obj())
