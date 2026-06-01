package chessengine.api.dto

import io.circe.{Decoder, Encoder}

final case class MoveRequest(move: String) derives Encoder, Decoder
