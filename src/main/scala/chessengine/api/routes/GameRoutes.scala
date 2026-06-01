package chessengine.api.routes

import chessengine.game.GameRegistry

import org.http4s.HttpRoutes
import org.http4s.dsl.io.*
import cats.effect.IO

private[api] class GameRoutes(registry: GameRegistry):
  def routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "games" =>
      println(registry.toString)
      ???
  }
