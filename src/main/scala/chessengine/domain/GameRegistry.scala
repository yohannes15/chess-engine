package chessengine.domain

import cats.effect.kernel.Ref
import cats.effect.IO
import java.util.UUID

private class GameRegistry(registry: Ref[IO, Map[UUID, GameState]]):
  def create(gameId: UUID = UUID.randomUUID()): IO[UUID] =
    registry.modify(games =>
      (games.updated(gameId, GameState.initial), gameId)
    )

  def lookup(gameId: UUID): IO[Option[GameState]] =
    registry.get.map(_.get(gameId))

  def applyMove(gameId: UUID, move: Move): IO[Option[GameState]] =
    registry.modify { games =>
      games.get(gameId) match
        case Some(gs) =>
          val nextGS = gs.applyMove(move)
          (games.updated(gameId, nextGS), Some(nextGS))
        case None => (games, None)
    }
