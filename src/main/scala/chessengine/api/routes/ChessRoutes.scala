package chessengine.api.routes

import org.http4s.HttpRoutes
import org.http4s.dsl.io.*
import cats.effect.IO
import org.http4s.circe.CirceEntityDecoder.*
import org.http4s.circe.jsonEncoder
import chessengine.engine.Search
import chessengine.api.dto.BestMoveRequest
import chessengine.domain.Fen
import cats.implicits.*
import io.circe.Json
import io.circe.syntax.*
import chessengine.api.dto.BestMoveResponse
import chessengine.engine.SearchRes.*
import chessengine.api.dto.CheckMateResponse
import chessengine.api.dto.StaleMateResponse

private[api] class ChessRoutes(val search: Search):
  def routes: HttpRoutes[IO] =
    HttpRoutes.of[IO] {
      case GET -> Root / "health"         => Ok("Healthy!")
      case r @ POST -> Root / "best-move" =>
        for
          request <- r.as[BestMoveRequest]
          state = Fen.parse(request.fen).fold(
            errors =>
              BadRequest(body =
                Json.obj(
                  "message" -> Json.fromString("invalid fen string"),
                  "errors" -> Json.arr(errors.toList.asJson)
                )
              ),
            state =>
              search.bestMove(state, request.depth) match
                case BestMove(mv, score) => BestMoveResponse(mv, score)
                case CheckMate => CheckMateResponse(state.color.opposite)
                case StaleMate => StaleMateResponse()
          )
        yield (???)
    }
