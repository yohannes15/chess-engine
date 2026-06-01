package chessengine.api.dto

import io.circe.Encoder
import java.util.UUID

final case class NewGameResponse(
    uuid: UUID,
    fen: String
) derives Encoder
