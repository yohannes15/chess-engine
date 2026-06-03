import { useState } from "react"
import { Chessboard } from "react-chessboard"

interface BoardProps {
  fen: string
  legalMoves: string[]
  onMove: (uci: string) => void
  orientation?: "white" | "black"
}

const HIGHLIGHT_DESTINATION: React.CSSProperties = {
  background: "radial-gradient(circle, rgba(0,0,0,0.15) 25%, transparent 25%)",
  borderRadius: "50%",
}

const HIGHLIGHT_SELECTED: React.CSSProperties = {
  background: "rgba(255, 215, 0, 0.5)",
}

export function Board({ fen, legalMoves, onMove, orientation = "white" }: BoardProps) {
  const [selected, setSelected] = useState<string | null>(null)

  const destinations: string[] = selected
    ? [...new Set(legalMoves.filter(m => m.startsWith(selected)).map(m => m.slice(2, 4)))]
    : []

  const squareStyles: Record<string, React.CSSProperties> = {
    ...Object.fromEntries(destinations.map(sq => [sq, HIGHLIGHT_DESTINATION])),
    ...(selected ? { [selected]: HIGHLIGHT_SELECTED } : {}),
  }

  function tryMove(from: string, to: string): boolean {
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
          if (selected && selected !== square && tryMove(selected, square)) return
          setSelected(legalMoves.some(m => m.startsWith(square)) ? square : null)
        },
        onPieceDrop: ({ sourceSquare, targetSquare }) =>
          targetSquare ? tryMove(sourceSquare, targetSquare) : false,
      }}
    />
  )
}
