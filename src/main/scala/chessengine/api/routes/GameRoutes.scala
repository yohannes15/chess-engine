package chessengine.api.routes

import chessengine.game.GameRegistry
import chessengine.api.dto.NewGameResponse
import chessengine.domain.Fen

import org.http4s.HttpRoutes
import org.http4s.dsl.io.*
import cats.effect.IO
import org.http4s.circe.CirceEntityEncoder.circeEntityEncoder

private[api] class GameRoutes(reg: GameRegistry):
  def routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case POST -> Root / "games" =>
      for
        (uuid, state) <- reg.create
        response <- Ok(NewGameResponse(uuid, Fen.write(state)))
      yield response
  }
