const FILES = "abcdefgh"

export function pieceAt(fen: string, square: string): string | null {
  const placement = fen.split(" ")[0]
  if (!placement) return null
  const file = FILES.indexOf(square[0] ?? "")
  const rank = Number(square[1])

  if (file < 0 || rank < 1 || rank > 8) return null

  const row = placement.split("/")[8 - rank]
  if (!row) return null

  let currentFile = 0
  for (const char of row) {
    if (/\d/.test(char)) {
      currentFile += Number(char)
    } else {
      if (currentFile === file) return char
      currentFile += 1
    }
  }

  return null
}

export function occupiedSquares(fen: string): Set<string> {
  const occupied = new Set<string>()

  for (const file of FILES) {
    for (let rank = 1; rank <= 8; rank += 1) {
      const square = `${file}${rank}`
      if (pieceAt(fen, square)) occupied.add(square)
    }
  }

  return occupied
}
