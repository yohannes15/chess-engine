package chessengine.api

import cats.effect.IOApp
import cats.effect.IO
import cats.implicits.*
import com.comcast.ip4s.*
import org.http4s.ember.server.EmberServerBuilder
import chessengine.engine.Search
import chessengine.engine.TranspositionTable as TTable
import org.http4s.HttpApp
import chessengine.api.routes.ChessRoutes
import chessengine.api.routes.GameRoutes
import chessengine.game.GameRegistry

object Main extends IOApp.Simple:
  def run: IO[Unit] =
    for
      _ <- IO.println("Starting Chess API ...")
      tt <- IO(TTable(250))
      gameRegistry <- GameRegistry.initial
      app: HttpApp[IO] = (
        ChessRoutes(Search(tt)).routes.orNotFound <+>
          GameRoutes(gameRegistry).routes.orNotFound
      )
      _ <- EmberServerBuilder
        .default[IO]
        .withHost(ipv4"0.0.0.0")
        .withPort(port"8080")
        .withHttpApp(app)
        .build // returns a Resource[IO, Server]. safe container
        .useForever
    yield ()
