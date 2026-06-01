package chessengine.api.routes

import cats.effect.IO
import org.http4s.*
import org.http4s.implicits.uri
import org.http4s.circe.CirceEntityDecoder.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.client.dsl.io.*
import io.circe.Json
import chessengine.TestHelpers.startingFen
import chessengine.game.GameRegistry

class GameRoutesSuite extends munit.CatsEffectSuite:

  private def makeApp: IO[HttpApp[IO]] =
    GameRegistry.initial.map(reg => GameRoutes(reg).routes.orNotFound)

  private def createGame(app: HttpApp[IO]): IO[String] =
    for
      response <- app.run(Request[IO](Method.POST, uri"games"))
      body <- response.as[Json]
      uuid = body.hcursor.downField("uuid").as[String].toOption
    yield (uuid.get)

  test("POST /games returns 200 with uuid and starting fen") {
    for
      app <- makeApp
      resp <- app.run(Request[IO](Method.POST, uri"/games"))
      body <- resp.as[Json]
      cur = body.hcursor
    yield
      assertEquals(resp.status, Status.Ok)
      assert(cur.downField("uuid").succeeded)
      assertEquals(cur.downField("fen").as[String], Right(startingFen))
  }

  test("POST /games returns different UUIDs") {
    for
      app <- makeApp
      uuid1 <- createGame(app)
      uuid2 <- createGame(app)
    yield assertNotEquals(uuid1, uuid2)
  }

  test("GET /games/:id returns 200 with fen, turn, and legal moves") {
    for
      app <- makeApp
      uuid <- createGame(app)
      resp <-
        app.run(Request[IO](Method.GET, Uri.unsafeFromString(s"/games/$uuid")))
      body <- resp.as[Json]
      cur = body.hcursor
    yield
      assertEquals(resp.status, Status.Ok)
      assertEquals(cur.downField("fen").as[String], Right(startingFen))
      assertEquals(cur.downField("turn").as[String], Right("white"))
      assert(cur.downField("legal_moves").as[List[String]].exists(_.nonEmpty))
  }

  test("GET /games/:id returns 404 for unknown uuid") {
    for
      app <- makeApp
      resp <- app.run(Request[IO](
        Method.GET,
        uri"/games/00000000-0000-0000-0000-000000000000"
      ))
    yield assertEquals(resp.status, Status.NotFound)
  }

  test("POST /games/:id/move with legal move returns 200 and updated state") {
    for
      app <- makeApp
      uuid <- createGame(app)
      body = Json.obj("move" -> Json.fromString("e2e4"))
      req = Method.POST(body, Uri.unsafeFromString(s"/games/$uuid/move"))
      resp <- app.run(req)
      json <- resp.as[Json]
      cur = json.hcursor
    yield
      assertEquals(resp.status, Status.Ok)
      assertEquals(cur.downField("turn").as[String], Right("black"))
      assert(cur.downField("fen").as[String].isRight)
      assert(cur.downField("legal_moves").as[List[String]].exists(_.nonEmpty))
  }

  test("POST /games/:id/move with illegal move returns 400") {
    for
      app <- makeApp
      uuid <- createGame(app)
      body = Json.obj("move" -> Json.fromString("e2e5"))
      req = Method.POST(body, Uri.unsafeFromString(s"/games/$uuid/move"))
      resp <- app.run(req)
    yield assertEquals(resp.status, Status.BadRequest)
  }

  test("POST /games/:id/move with unknown uuid returns 404") {
    for
      app <- makeApp
      body = Json.obj("move" -> Json.fromString("e2e4"))
      req =
        Method.POST(body, uri"/games/00000000-0000-0000-0000-000000000000/move")
      resp <- app.run(req)
    yield assertEquals(resp.status, Status.NotFound)
  }
