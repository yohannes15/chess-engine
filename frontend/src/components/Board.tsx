import { useState } from "react"
import { Chessboard } from "react-chessboard"
import { occupiedSquares } from "../chess/fen"

export interface LastMove {
  uci: string
  wasCapture: boolean
}

interface BoardProps {
  fen: string
  legalMoves: string[]
  onMove: (uci: string) => void
  orientation?: "white" | "black"
  lastMove?: LastMove | null
  disabled?: boolean
}

const HIGHLIGHT_DESTINATION: React.CSSProperties = {
  background: "radial-gradient(circle, rgba(0,0,0,0.15) 25%, transparent 25%)",
  borderRadius: "50%",
}

const HIGHLIGHT_CAPTURE_DESTINATION: React.CSSProperties = {
  boxShadow: "inset 0 0 0 5px rgba(201, 162, 39, 0.55)",
  borderRadius: "50%",
}

const HIGHLIGHT_SELECTED: React.CSSProperties = {
  background: "rgba(255, 215, 0, 0.5)",
}

const HIGHLIGHT_LAST_MOVE: React.CSSProperties = {
  background: "rgba(201, 162, 39, 0.35)",
}

const HIGHLIGHT_LAST_CAPTURE: React.CSSProperties = {
  background: "rgba(224, 82, 82, 0.35)",
  boxShadow: "inset 0 0 0 4px rgba(224, 82, 82, 0.5)",
}

export function Board({
  fen,
  legalMoves,
  onMove,
  orientation = "white",
  lastMove,
  disabled = false,
}: BoardProps) {
  const [selected, setSelected] = useState<string | null>(null)
  const occupied = occupiedSquares(fen)

  const destinations: string[] = selected
    ? [...new Set(legalMoves.filter(m => m.startsWith(selected)).map(m => m.slice(2, 4)))]
    : []

  const lastMoveFrom = lastMove?.uci.slice(0, 2)
  const lastMoveTo = lastMove?.uci.slice(2, 4)

  const squareStyles: Record<string, React.CSSProperties> = {
    ...(lastMoveFrom ? { [lastMoveFrom]: HIGHLIGHT_LAST_MOVE } : {}),
    ...(lastMoveTo
      ? { [lastMoveTo]: lastMove.wasCapture ? HIGHLIGHT_LAST_CAPTURE : HIGHLIGHT_LAST_MOVE }
      : {}),
    ...Object.fromEntries(destinations.map(sq => [
      sq,
      occupied.has(sq) ? HIGHLIGHT_CAPTURE_DESTINATION : HIGHLIGHT_DESTINATION,
    ])),
    ...(selected ? { [selected]: HIGHLIGHT_SELECTED } : {}),
  }

  function tryMove(from: string, to: string): boolean {
    if (disabled) return false
    const move = legalMoves.find(m => m === `${from}${to}q`) ??
                 legalMoves.find(m => m === `${from}${to}`)
    if (!move) return false
    onMove(move)
    setSelected(null)
    return true
  }

  return (
    <Chessboard
      options={{
        position: fen,
        boardOrientation: orientation,
        squareStyles,
        onSquareClick: ({ square }) => {
          if (disabled) return
          if (selected && selected !== square && tryMove(selected, square)) return
          setSelected(legalMoves.some(m => m.startsWith(square)) ? square : null)
        },
        onPieceDrop: ({ sourceSquare, targetSquare }) =>
          targetSquare ? tryMove(sourceSquare, targetSquare) : false,
      }}
    />
  )
}
