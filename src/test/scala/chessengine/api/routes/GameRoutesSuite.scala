package chessengine.api.routes

import cats.effect.IO
import org.http4s.*
import org.http4s.implicits.uri
import org.http4s.circe.CirceEntityDecoder.*
import io.circe.Json
import chessengine.TestHelpers.startingFen
import chessengine.game.GameRegistry

class GameRoutesSuite extends munit.CatsEffectSuite:

  private def makeApp: IO[HttpApp[IO]] =
    GameRegistry.initial.map(reg => GameRoutes(reg).routes.orNotFound)

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
      req = Request[IO](Method.POST, uri"/games")
      body1 <- app.run(req).flatMap(_.as[Json])
      body2 <- app.run(req).flatMap(_.as[Json])
      uuid1 = body1.hcursor.downField("uuid").as[String]
      uuid2 = body2.hcursor.downField("uuid").as[String]
    yield assertNotEquals(uuid1, uuid2)
  }
