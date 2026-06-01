package chessengine.api.routes

import org.http4s.HttpRoutes
import org.http4s.dsl.io.*
import cats.effect.IO
import org.http4s.circe.CirceEntityDecoder.*
import org.http4s.circe.CirceEntityEncoder.circeEntityEncoder
import chessengine.engine.Search
import chessengine.domain.Fen
import cats.implicits.*
import io.circe.Json
import io.circe.syntax.*
import chessengine.api.dto.{
  BestMoveRequest, BestMoveResponse, CheckMateResponse, StaleMateResponse,
  ValidateMoveRequest, ValidateMoveResponse
}
import chessengine.engine.SearchRes.*
import chessengine.logic.MoveGenerator.allLegalMoves

private[api] class ChessRoutes(search: Search):
  def routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "health"         => Ok("Healthy!")
    case r @ POST -> Root / "best-move" =>
      for
        request <- r.as[BestMoveRequest]
        response <- Fen.parse(request.fen).fold(
          errors =>
            BadRequest(body =
              Json.obj(
                "message" -> Json.fromString("invalid fen string"),
                "errors" -> Json.arr(errors.toList.asJson)
              )
            ),
          state =>
            search.bestMove(state, request.depth) match
              case BestMove(mv, score) => Ok(BestMoveResponse(mv, score))
              case CheckMate => Ok(CheckMateResponse(state.color.opposite))
              case StaleMate => Ok(StaleMateResponse())
        )
      yield (response)

    case r @ POST -> Root / "validate-move" =>
      for
        request <- r.as[ValidateMoveRequest]
        response <- Fen.parse(request.fen).fold(
          errors =>
            BadRequest(body =
              Json.obj(
                "message" -> Json.fromString("invalid fen string"),
                "errors" -> Json.arr(errors.toList.asJson)
              )
            ),
          state =>
            Ok(
              ValidateMoveResponse(valid =
                allLegalMoves(state).exists(_.toUCI ==
                  request.move.toLowerCase)
              )
            )
        )
      yield (response)
  }
