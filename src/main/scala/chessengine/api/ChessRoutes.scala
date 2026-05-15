package chessengine.api

import org.http4s.HttpRoutes
import org.http4s.dsl.io.*
import cats.effect.IO
import chessengine.engine.Search

private class ChessRoutes(val search: Search):
  def routes: HttpRoutes[IO] =
    HttpRoutes.of[IO] {
      case GET -> Root / "health" => Ok("Healthy!")
    }
