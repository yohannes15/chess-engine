package chessengine.api.routes

import chessengine.game.GameRegistry
import org.http4s.HttpRoutes
import cats.effect.IO

private[api] class GameRoutes(registry: GameRegistry):
  def routes: HttpRoutes[IO] =
    ???
