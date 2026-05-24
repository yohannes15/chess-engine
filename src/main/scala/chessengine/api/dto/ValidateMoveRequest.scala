package chessengine.api.dto

import io.circe.Decoder

final case class ValidateMoveRequest(fen: String, move: String)

object ValidateMoveRequest:
  given validateMoveRequestDecoder: Decoder[ValidateMoveRequest] =
    Decoder.forProduct2("fen", "move")(ValidateMoveRequest(_, _))
