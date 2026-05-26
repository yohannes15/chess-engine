package chessengine.api.routes

import cats.effect.IO
import org.http4s.*
import org.http4s.implicits.{uri}
import chessengine.engine.{Search, TranspositionTable}
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.circe.CirceEntityDecoder.*
import org.http4s.client.dsl.io._ // implicit conversion to MethodOps[IO]
import chessengine.TestHelpers.startingFen
import io.circe.Json

class ChessRoutesSuite extends munit.CatsEffectSuite:
  val app = ChessRoutes(Search(TranspositionTable(250))).routes.orNotFound

  test("health returns 200") {
    // val request: Request[IO](Method.GET, uri"/health")
    val request: Request[IO] = Method.GET(uri"/health")
    val respStatus: IO[Status] = app.run(request).map(_.status)
    assertIO(respStatus, Status.Ok)
  }

  test("best-move with valid FEN returns 200 and a move") {
    val body = Json.obj(
      "fen" -> Json.fromString(startingFen),
      "depth" -> Json.fromInt(1)
    )
    val request = Method.POST(body, uri"/best-move")
    val response = app.run(request)
    response.flatMap { response =>
      assertEquals(response.status, Status.Ok)
      response.as[Json].map { json =>
        assert(json.hcursor.get[String]("move").isRight)
        assert(json.hcursor.get[Int]("score").isRight)
      }
    }
  }

  test("best-move with invalid FEN returns 400") {
    val body = Json.obj(
      "fen" -> Json.fromString("not a valid fen"),
      "depth" -> Json.fromInt(0)
    )
    val request = Method.POST(body, uri"/best-move")
    app.run(request).flatMap { response =>
      assertEquals(response.status, Status.BadRequest)
      response.as[Json].map { json =>
        assertEquals(
          json.hcursor.get[String]("message"),
          Right("invalid fen string")
        )
        assert(json.hcursor.get[Json]("errors").isRight)
      }
    }
  }

  test("best-move on checkmate position returns winner") {
    val body = Json.obj(
      "fen" -> Json.fromString("7k/5KQ1/8/8/8/8/8/8 b - -"),
      "depth" -> Json.fromInt(0)
    )
    val request = Method.POST(body, uri"/best-move")
    app.run(request).flatMap { response =>
      assertEquals(response.status, Status.Ok)
      response.as[Json].map { json =>
        assertEquals(json.hcursor.get[String]("winner"), Right("white"))
      }
    }
  }

  test("best-move on stalemate position returns empty response") {
    val body = Json.obj(
      "fen" -> Json.fromString("7k/5Q2/8/8/8/8/8/7K b - -"),
      "depth" -> Json.fromInt(0)
    )
    val request = Method.POST(body, uri"/best-move")
    app.run(request).flatMap { response =>
      assertEquals(response.status, Status.Ok)
      response.as[Json].map { json => assertEquals(json, Json.obj()) }
    }
  }

  test("validate-move with legal move returns valid true") {
    val body = Json.obj(
      "fen" -> Json.fromString(startingFen),
      "move" -> Json.fromString("e2e4")
    )
    val request = Method.POST(body, uri"/validate-move")
    app.run(request).flatMap { response =>
      assertEquals(response.status, Status.Ok)
      response.as[Json].map { json =>
        assertEquals(json.hcursor.get[Boolean]("valid"), Right(true))
      }
    }
  }

  test("validate-move with illegal move returns valid false") {
    val body = Json.obj(
      "fen" -> Json.fromString(startingFen),
      "move" -> Json.fromString("e2e5")
    )
    val request = Method.POST(body, uri"/validate-move")
    app.run(request).flatMap { response =>
      assertEquals(response.status, Status.Ok)
      response.as[Json].map { json =>
        assertEquals(json.hcursor.get[Boolean]("valid"), Right(false))
      }
    }
  }

  test("validate-move with invalid FEN returns 400") {
    val body = Json.obj(
      "fen" -> Json.fromString("not a valid fen"),
      "move" -> Json.fromString("e2e4")
    )
    val request = Method.POST(body, uri"/validate-move")
    app.run(request).flatMap { response =>
      assertEquals(response.status, Status.BadRequest)
      response.as[Json].map { json =>
        assertEquals(
          json.hcursor.get[String]("message"),
          Right("invalid fen string")
        )
        assert(json.hcursor.get[Json]("errors").isRight)
      }
    }
  }
