package chessengine.api

import cats.effect.IOApp
import cats.effect.IO
import cats.implicits.*
import com.comcast.ip4s.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.CORS
import org.http4s.server.staticcontent.{fileService, FileService}
import org.http4s.{HttpApp, HttpRoutes, Method, Response, StaticFile, Status}
import chessengine.engine.Search
import chessengine.engine.TranspositionTable as TTable
import chessengine.api.routes.ChessRoutes
import chessengine.api.routes.GameRoutes
import chessengine.game.GameRegistry
import fs2.io.file.Path as Fs2Path

object Main extends IOApp.Simple:

  private def frontendRoutes(distPath: String): HttpRoutes[IO] =
    val staticFiles = fileService[IO](FileService.Config[IO](systemPath = distPath))
    val indexPath = Fs2Path(distPath) / "index.html"
    val fallback = HttpRoutes.of[IO] {
      case req if req.method == Method.GET =>
        StaticFile.fromPath[IO](indexPath, Some(req))
          .getOrElseF(Response[IO](Status.NotFound).pure[IO])
    }
    staticFiles <+> fallback

  def run: IO[Unit] =
    for
      _ <- IO.println("Starting Chess API ...")
      tt <- IO(TTable(250))
      gameRegistry <- GameRegistry.initial
      frontendDist <- IO(sys.env.getOrElse("FRONTEND_DIST", "frontend/dist"))

      apiRoutes = ChessRoutes(Search(tt)).routes <+> GameRoutes(gameRegistry).routes
      app: HttpApp[IO] = CORS.policy.withAllowOriginAll.httpApp(
        (apiRoutes <+> frontendRoutes(frontendDist)).orNotFound
      )
      _ <- EmberServerBuilder
        .default[IO]
        .withHost(ipv4"0.0.0.0")
        .withPort(port"8080")
        .withHttpApp(app)
        .build
        .useForever
    yield ()
