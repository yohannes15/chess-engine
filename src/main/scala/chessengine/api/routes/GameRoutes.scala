package chessengine.api.routes

import chessengine.game.GameRegistry
import chessengine.api.dto.NewGameResponse
import chessengine.domain.Fen

import org.http4s.HttpRoutes
import org.http4s.dsl.io.*
import cats.effect.IO
import org.http4s.circe.CirceEntityEncoder.circeEntityEncoder
import org.http4s.circe.CirceEntityDecoder.circeEntityDecoder
import chessengine.api.dto.GameStateResponse
import chessengine.logic.MoveGenerator.*
import chessengine.api.dto.MoveRequest
import java.util.UUID
import org.http4s.Response
import chessengine.domain.{GameState, Move}

private[api] class GameRoutes(reg: GameRegistry):

  private def handleMove(id: UUID, mv: Move): IO[Response[IO]] =
    reg.applyMove(id, mv).flatMap {
      case None       => NotFound("invalid game uuid")
      case Some(next) => Ok(gameStateResponse(next))
    }

  private def gameStateResponse(gs: GameState): GameStateResponse =
    GameStateResponse(
      fen = Fen.write(gs),
      turn = gs.color.toString,
      legalMoves = allLegalMoves(gs).map(_.toUCI)
    )

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
          case Some(gs) => Ok(gameStateResponse(gs))
          case None     => NotFound("invalid game uuid")
      yield response

    case req @ POST -> Root / "games" / UUIDVar(id) / "move" =>
      for
        mvReq <- req.as[MoveRequest]
        gameState <- reg.lookup(id)
        response <- gameState match
          case None     => NotFound("invalid game uuid")
          case Some(gs) =>
            allLegalMoves(gs).find(_.toUCI == mvReq.move.toLowerCase) match
              case None     => BadRequest("illegal move")
              case Some(mv) => handleMove(id, mv)
      yield response
  }
