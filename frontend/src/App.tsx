import { useState } from "react"
import { api } from "./api/client"
import type { GameState } from "./api/client"
import { Landing } from "./components/Landing"
import { GameView } from "./components/GameView"

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
    <div className="shell">
      <header className="app-header">
        <span className="app-title">Chess Engine</span>
        <span className="app-subtitle">Negamax · Alpha-Beta · Scala 3</span>
      </header>

      {error && <p className="error-banner">{error}</p>}

      {!game ? (
        <Landing loading={loading} onStart={startGame} />
      ) : (
        <GameView
          game={game}
          loading={loading}
          onMove={handleMove}
          onNewGame={startGame}
        />
      )}
    </div>
  )
}

export default App
