# Chess Engine

A purely functional chess engine and HTTP API in Scala 3, Cats Effect, and Http4s.

## Portfolio

A purely functional chess engine implementation in Scala 3 that demonstrates advanced functional programming concepts and concurrent system design. Implements perft-validated move generation, FEN notation parsing, alpha-beta search with transposition tables and Zobrist hashing, piece-square evaluation, and a stateless HTTP API. Built with Cats Effect for side-effect management and Http4s for the REST API layer.

The goal is to create a production-ready chess library similar to Lichess's scalachess, with WebSocket support for live games and a stateless HTTP API for move validation and engine suggestions.

## Status

Core engine is complete and tested:

- **Domain model** — Piece, Board, Square, Move, GameState, FEN notation, castling rights
- **Move generation** — pseudo-legal + legal moves, perft-validated across 4 positions (depths 1–4)
- **Search** — negamax with alpha-beta pruning, transposition table, Zobrist hashing
- **Evaluation** — material counting + piece-square tables
- **HTTP API** — health check, best-move suggestion, move validation
- **Multiplayer backend** — GameRegistry with game creation, state lookup, move application
- **PGN parser** — tag parser, SAN resolver with disambiguation, SAN encoder
- **Tests** — 254 tests across 8 suites, all passing

## Quick start

```sh
sbt compile          # compile all sources
sbt test             # run all tests
sbt ~reStart         # hot-reload the HTTP server on port 8080
sbt assembly         # fat JAR
```

## Stack

| | |
|---|---|
| Language | Scala 3.8.2 |
| Build | sbt 1.12.8 |
| Effects | Cats Effect 3, Http4s 0.23, Circe 0.14 |
| Test | MUnit + munit-cats-effect |
| Formatter | scalafmt 3.10.7 (scala3 dialect, remove-optional-braces) |
| Plugins | sbt-tpolecat (warnings-as-errors), sbt-revolver, sbt-assembly |

## Project structure

```
src/main/scala/chessengine/
├── domain/       Piece, Board, Square, Move, GameState, Fen, CastlingRights, Color, Role, Side
├── logic/        MoveGenerator — pseudo-legal + legal move generation, check/stalemate detection
├── engine/       Search (negamax + alpha-beta), Evaluation (material + PST),
│                 TranspositionTable, Zobrist hashing, Score, TTEntry
├── game/         GameRegistry — Ref-backed multiplayer game state
└── api/          Http4s server (port 8080), routes (health, best-move, validate-move,
                  game create/lookup/move), DTOs

src/test/scala/chessengine/
├── domain/       SquareSuite (139), FenSuite (23), BoardSuite (9), GameStateSuite (23)
├── logic/        MoveGeneratorSuite (14) — perft tests
├── engine/       SearchSuite (4), EvaluationSuite (6)
└── api/          ChessRoutesSuite (8), GameRoutesSuite (2)
```

## Architecture notes

- `Square` is an opaque type wrapping `Int` (0–63), row-major: `rank * 8 + file`. a1=0, h8=63.
- `Board` is a `Vector[Option[Piece]]` of length 64.
- `GameState.applyMove` uses incremental Zobrist hashing — the hash updates on each move rather than recomputing.
- `TranspositionTable` is a fixed-size array with a dual-bucket strategy (depth-preferred + recent-replace per bucket). Size must be a power of 2.
- `Search` uses negamax with alpha-beta pruning. Checkmate scores are ply-adjusted before TT storage.
- `MoveGenerator.isLegal` applies the move then checks if the moving player's king is attacked.
- `GameRegistry` wraps a `Ref[IO, Map[UUID, GameState]]` for thread-safe multiplayer game storage.

## Roadmap

| Priority | What | Status |
|---|---|---|
| — | Multiplayer backend (GameRegistry, game routes, PGN parser, Fen.write) | Done |
| 1 | Frontend (visual board, WebSockets via FS2, game clock) | Next |
| 2 | Bitboard optimization | Later |

## License

MIT
