package chessengine.api.routes

import org.http4s.HttpRoutes
import org.http4s.dsl.io.*
import cats.effect.IO
import org.http4s.circe.CirceEntityDecoder.*
import chessengine.engine.Search
import chessengine.api.dto.BestMoveRequest

private[api] class ChessRoutes(val search: Search):
  def routes: HttpRoutes[IO] =
    HttpRoutes.of[IO] {
      case GET -> Root / "health"         => Ok("Healthy!")
      case r @ POST -> Root / "best-move" =>
        for
          request <- r.as[BestMoveRequest]
          x = search.bestMove(???, request.depth)
          response <- Ok("TBD")
        yield response

    }
