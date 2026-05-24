package chessengine.api.dto

import io.circe.Encoder
import io.circe.Json

final case class StaleMateResponse()

object StaleMateResponse:
  given stalemateResponseEncoder: Encoder[StaleMateResponse] =
    Encoder.instance(_ => Json.obj())
