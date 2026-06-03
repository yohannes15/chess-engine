import { useState } from "react"
import { api } from "../api/client"
import type { BestMoveResult, GameState } from "../api/client"
import { pieceAt } from "../chess/fen"
import { Landing } from "../components/Landing"
import { GameView } from "../components/GameView"
import type { GameOver } from "../components/GameView"
import type { LastMove } from "../components/Board"

const ENGINE_DEPTH = 3

function isBestMove(result: BestMoveResult): result is { move: string; score: number } {
  return "move" in result
}

function gameOverFrom(result: BestMoveResult): GameOver {
  return "winner" in result
    ? { kind: "checkmate", winner: result.winner }
    : { kind: "stalemate" }
}

function moveMetadata(fenBeforeMove: string, uci: string): LastMove {
  const from = uci.slice(0, 2)
  const to = uci.slice(2, 4)
  const movingPiece = pieceAt(fenBeforeMove, from)
  const targetPiece = pieceAt(fenBeforeMove, to)
  const isEnPassant = movingPiece?.toLowerCase() === "p" && from[0] !== to[0] && !targetPiece

  return {
    uci,
    wasCapture: Boolean(targetPiece) || isEnPassant,
  }
}

export function PlayerVsEngine() {
  const [gameId, setGameId] = useState<string | null>(null)
  const [game, setGame] = useState<GameState | null>(null)
  const [starting, setStarting] = useState(false)
  const [engineThinking, setEngineThinking] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [lastMove, setLastMove] = useState<LastMove | null>(null)
  const [gameOver, setGameOver] = useState<GameOver | null>(null)

  async function resolveGameOver(state: GameState): Promise<GameOver | null> {
    if (state.legal_moves.length > 0) return null
    return gameOverFrom(await api.bestMove(state.fen, 1))
  }

  async function startGame() {
    setStarting(true)
    setError(null)
    setLastMove(null)
    setGameOver(null)
    try {
      const { uuid } = await api.createGame()
      const state = await api.getGame(uuid)
      setGameId(uuid)
      setGame(state)
    } catch (e) {
      setError(e instanceof Error ? e.message : "Failed to start game")
    } finally {
      setStarting(false)
    }
  }

  async function handleMove(uci: string) {
    if (!gameId || !game || engineThinking || gameOver) return
    setEngineThinking(true)
    setError(null)
    try {
      const playerMove = moveMetadata(game.fen, uci)
      const afterPlayerMove = await api.applyMove(gameId, uci)
      setLastMove(playerMove)
      setGame(afterPlayerMove)

      const playerGameOver = await resolveGameOver(afterPlayerMove)
      if (playerGameOver) {
        setGameOver(playerGameOver)
        return
      }

      const engineResult = await api.bestMove(afterPlayerMove.fen, ENGINE_DEPTH)
      if (!isBestMove(engineResult)) {
        setGameOver(gameOverFrom(engineResult))
        return
      }

      const engineMove = moveMetadata(afterPlayerMove.fen, engineResult.move)
      const afterEngineMove = await api.applyMove(gameId, engineResult.move)
      setLastMove(engineMove)
      setGame(afterEngineMove)
      setGameOver(await resolveGameOver(afterEngineMove))
    } catch (e) {
      setError(e instanceof Error ? e.message : "Move failed")
    } finally {
      setEngineThinking(false)
    }
  }

  const busy = starting || engineThinking

  return (
    <>
      {error && <p className="error-banner">{error}</p>}

      {!game ? (
        <Landing loading={starting} onStart={startGame} />
      ) : (
        <GameView
          game={game}
          starting={starting}
          busy={busy}
          lastMove={lastMove}
          gameOver={gameOver}
          onMove={handleMove}
          onNewGame={startGame}
        />
      )}
    </>
  )
}
