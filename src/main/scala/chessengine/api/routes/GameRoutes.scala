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
import io.circe.Json

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
        response <- gameState.match
          case Some(gs) =>
            Ok(GameStateResponse(
              fen = Fen.write(gs),
              turn = gs.color.toString.toLowerCase,
              legalMoves = MoveGenerator.allLegalMoves(gs).map(mv => mv.toUCI)
            ))
          case None => BadRequest(
              Json.obj(
                "message" -> Json.fromString("id: UUID not valid"),
                "errors" -> Json.arr()
              )
            )
      yield ???
  }
