package chessengine.api.routes

import chessengine.game.GameRegistry
import chessengine.api.dto.NewGameResponse
import chessengine.domain.Fen

import org.http4s.HttpRoutes
import org.http4s.dsl.io.*
import cats.effect.IO
import org.http4s.circe.CirceEntityEncoder.circeEntityEncoder
import chessengine.api.dto.GameStateResponse
import chessengine.logic.MoveGenerator

private[api] class GameRoutes(reg: GameRegistry):
  def routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case POST -> Root / "games" =>
      for
        (uuid, state) <- reg.create
        response <- Ok(NewGameResponse(uuid, Fen.write(state)))
      yield response

    case GET -> Root / "games" / UUIDVar(id) =>
      for
        gameState <- reg.lookup(id)
        response <- gameState match
          case Some(gs) =>
            Ok(GameStateResponse(
              fen = Fen.write(gs),
              turn = gs.color.toString,
              legalMoves = MoveGenerator.allLegalMoves(gs).map(_.toUCI)
            ))
          case None => NotFound("invalid game uuid")
      yield response
  }
