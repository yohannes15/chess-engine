package chessengine.api.dto

import io.circe.Decoder

final case class BestMoveRequest(fen: String, depth: Int)

object BestMoveRequest:
  given bestMoveRequestDecoder: Decoder[BestMoveRequest] =
    Decoder.forProduct2("fen", "depth")(BestMoveRequest.apply)
