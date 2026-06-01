package chessengine.game

import cats.effect.kernel.Ref
import cats.effect.IO
import java.util.UUID
import chessengine.domain.{GameState, Move}

final class GameRegistry private (registry: Ref[IO, Map[UUID, GameState]]):
  def create: IO[(UUID, GameState)] =
    registry.modify { games =>
      val id = UUID.randomUUID()
      val gs = GameState.initial
      (games.updated(id, gs), (id, gs))
    }

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

object GameRegistry:
  def initial: IO[GameRegistry] =
    Ref.of[IO, Map[UUID, GameState]](Map.empty).map(ref => GameRegistry(ref))
