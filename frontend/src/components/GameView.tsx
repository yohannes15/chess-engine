import type { GameState } from "../api/client"
import { Board } from "./Board"

interface GameViewProps {
  game: GameState
  loading: boolean
  onMove: (uci: string) => void
  onNewGame: () => void
}

export function GameView({ game, loading, onMove, onNewGame }: GameViewProps) {
  return (
    <div className="game-container">
      <div className="turn-badge">
        <span className={`turn-dot ${game.turn}`} />
        {game.turn === "white" ? "White to move" : "Black to move"}
      </div>
      <div style={{ width: "100%" }}>
        <Board
          fen={game.fen}
          legalMoves={game.legal_moves}
          onMove={onMove}
        />
      </div>
      <button onClick={onNewGame} disabled={loading}>
        {loading ? "Starting..." : "New Game"}
      </button>
    </div>
  )
}
