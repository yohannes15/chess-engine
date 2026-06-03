import { useState } from "react"
import type { GameState } from "../api/client"
import { Board } from "./Board"
import type { LastMove } from "./Board"

export type GameOver =
  | { kind: "checkmate"; winner: "white" | "black" }
  | { kind: "stalemate" }

interface GameViewProps {
  game: GameState
  starting: boolean
  busy: boolean
  lastMove: LastMove | null
  gameOver: GameOver | null
  onMove: (uci: string) => void
  onNewGame: () => void
}

export function GameView({
  game,
  starting,
  busy,
  lastMove,
  gameOver,
  onMove,
  onNewGame,
}: GameViewProps) {
  const [orientation, setOrientation] = useState<"white" | "black">("white")
  const status = gameOver
    ? gameOver.kind === "checkmate"
      ? `Checkmate. ${gameOver.winner === "white" ? "White" : "Black"} wins.`
      : "Stalemate. Draw."
    : game.turn === "white"
      ? "White to move"
      : "Black to move"

  return (
    <div className="game-container">
      <div className="game-status-row">
        <div className="turn-badge">
          <span className={`turn-dot ${game.turn}`} />
          {status}
        </div>
      </div>

      {gameOver && <div className="game-over-card">{status}</div>}

      <div className="board-wrap">
        <Board
          fen={game.fen}
          legalMoves={game.legal_moves}
          onMove={onMove}
          orientation={orientation}
          lastMove={lastMove}
          disabled={busy || Boolean(gameOver)}
        />
      </div>
      <div className="game-actions">
        <button
          type="button"
          className="secondary-button"
          onClick={() => setOrientation(o => o === "white" ? "black" : "white")}
        >
          Flip Board
        </button>
        <button type="button" onClick={onNewGame} disabled={busy}>
          {starting ? "Starting..." : "New Game"}
        </button>
      </div>
    </div>
  )
}
