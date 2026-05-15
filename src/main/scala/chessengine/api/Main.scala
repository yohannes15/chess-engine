package chessengine.api

import cats.effect.IOApp
import cats.effect.IO
import com.comcast.ip4s.*
import org.http4s.ember.server.EmberServerBuilder
import chessengine.engine.Search
import chessengine.engine.TranspositionTable as TTable
import org.http4s.HttpApp
import cats.effect.kernel.Resource

object Main extends IOApp.Simple:
  def run: IO[Unit] =
    val ttResource: Resource[IO, TTable] =
      Resource.make[IO, TTable](IO(TTable(sizeInMB = 250)))(_ => IO.unit)

    /** Ember requires an HttpApp because it can't just "not respond" to a
      * browser. routes.orNotFound is a helper that says: "Try to use my routes.
      * If none of them match, return a 404 Not Found response automatically."
      */
    ttResource.use { tt =>
      for
        _ <- IO.println("Starting Chess API ...")
        app: HttpApp[IO] = ChessRoutes(Search(tt)).routes.orNotFound
        _ <- EmberServerBuilder
          .default[IO]
          .withHost(ipv4"0.0.0.0")
          .withPort(port"8080")
          .withHttpApp(app)
          .build // returns a Resource[IO, Server]. safe container
          .useForever
      yield ()
    }
