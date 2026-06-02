package chessengine.api.dto

import io.circe.Encoder

final case class ErrorResponse(message: String, errors: List[String] = List.empty) derives Encoder
