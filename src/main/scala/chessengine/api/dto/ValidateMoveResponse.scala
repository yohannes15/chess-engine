package chessengine.api.dto

import io.circe.Encoder

final case class ValidateMoveResponse(valid: Boolean)

object ValidateMoveResponse:
  given validateMoveResponseEncoder: Encoder[ValidateMoveResponse] =
    Encoder.forProduct1("valid")(_.valid)
