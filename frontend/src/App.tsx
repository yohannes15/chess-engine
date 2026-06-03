import { useState } from "react"
import { api } from "./api/client"
import type { GameState } from "./api/client"
import { Board } from "./components/Board"

function App() {
  const [gameId, setGameId] = useState<string | null>(null)
  const [game, setGame] = useState<GameState | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function startGame() {
    setLoading(true)
    setError(null)
    try {
      const { uuid } = await api.createGame()
      const state = await api.getGame(uuid)
      setGameId(uuid)
      setGame(state)
    } catch (e) {
      setError(e instanceof Error ? e.message : "Failed to start game")
    } finally {
      setLoading(false)
    }
  }

  async function handleMove(uci: string) {
    if (!gameId) return
    setError(null)
    try {
      const state = await api.applyMove(gameId, uci)
      setGame(state)
    } catch (e) {
      setError(e instanceof Error ? e.message : "Move failed")
    }
  }

  return (
    <div style={{ display: "flex", flexDirection: "column", alignItems: "center", padding: "2rem", gap: "1rem" }}>
      <h1>Chess Engine</h1>

      {error && <p style={{ color: "crimson" }}>{error}</p>}

      {!game ? (
        <button onClick={startGame} disabled={loading}>
          {loading ? "Starting..." : "New Game"}
        </button>
      ) : (
        <>
          <p>{game.turn === "white" ? "White to move" : "Black to move"}</p>
          <div style={{ width: "min(90vw, 560px)" }}>
            <Board
              fen={game.fen}
              legalMoves={game.legal_moves}
              onMove={handleMove}
            />
          </div>
          <button onClick={startGame} disabled={loading}>
            New Game
          </button>
        </>
      )}
    </div>
  )
}

export default App
