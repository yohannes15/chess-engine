package chessengine.api.dto

import io.circe.Encoder
import io.circe.derivation.Configuration
import io.circe.derivation.ConfiguredCodec

given Configuration = Configuration.default.withSnakeCaseMemberNames

final case class GameStateResponse(
    fen: String,
    turn: String,
    legalMoves: List[String]
) derives ConfiguredCodec
