import type { components } from "../types/api"
export type NewGame      = components["schemas"]["NewGameResponse"]
export type GameState    = components["schemas"]["GameStateResponse"]
export type BestMove     = components["schemas"]["BestMoveResponse"]
export type CheckMate    = components["schemas"]["CheckMateResponse"]
export type StaleMate    = components["schemas"]["StaleMateResponse"]
export type ApiError     = components["schemas"]["ErrorResponse"]
export type BestMoveResult = BestMove | CheckMate | StaleMate

const BASE = "http://localhost:8080"

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`${BASE}${path}`, {
    headers: { "Content-Type": "application/json", ...init?.headers },
    ...init,
  })
  if (!res.ok) {
    const err: ApiError = await res.json()
    throw new Error(err.message)
  }
  return res.json() as Promise<T>
}

export const api = {
  createGame: (): Promise<NewGame> =>
    request("/games", { method: "POST" }),

  getGame: (id: string): Promise<GameState> =>
    request(`/games/${id}`),

  applyMove: (id: string, move: string): Promise<GameState> =>
    request(`/games/${id}/move`, {
      method: "POST",
      body: JSON.stringify({ move }),
    }),

  bestMove: (fen: string, depth: number): Promise<BestMoveResult> =>
    request("/best-move", {
      method: "POST",
      body: JSON.stringify({ fen, depth }),
    }),
}

