# AGENTS.md — Chess Engine

## Stack

- **Language:** Scala 3.8.2  
- **Build:** sbt 1.12.8  
- **Effects:** Cats Effect 3, Http4s 0.23, Circe 0.14  
- **Test:** MUnit + munit-cats-effect (only a test skeleton exists)  
- **Formatter:** scalafmt 3.10.7 (`runner.dialect = scala3`, `rewrite.scala3.removeOptionalBraces.enabled = true`)  
- **Plugins:** sbt-tpolecat (warnings-as-errors), sbt-revolver, sbt-assembly  

## Developer commands

```sh
sbt compile          # compile all sources
sbt test             # run tests (currently just empty Main.scala)
sbt ~reStart         # hot-reload the HTTP server (sbt-revolver)
sbt assembly         # fat JAR (uses MergeStrategy.discard for module-info.class)
```

No CI pipeline is configured yet.

## Key directories

| Path | Contents |
|------|----------|
| `src/main/scala/chessengine/domain/` | Core types: `Piece`, `Board`, `Square`, `Move`, `GameState`, `Fen`, `CastlingRights`, `Role`, `Color`, `Side` |
| `src/main/scala/chessengine/logic/` | `MoveGenerator` — pseudo-legal + legal move generation, check/stalemate detection |
| `src/main/scala/chessengine/engine/` | `Search` (negamax + alpha-beta), `Evaluation` (material + PST), `TranspositionTable`, `Zobrist`, `Score`, `TTEntry` |
| `src/main/scala/chessengine/api/` | Http4s server (`Main.scala` on port 8080), `ChessRoutes` (health + best-move stub), DTOs |
| `src/test/` | Test skeleton only — no actual test cases exist yet |

## Architecture notes

- `Square` is an **opaque type** wrapping `Int` (0–63), row-major: `rank * 8 + file`. Zero-based: a1=0, h8=63.
- `Board` is a `Vector[Option[Piece]]` of length 64 (not yet bitboard-based despite README mentioning bitboards).
- `GameState.applyMove` uses **incremental Zobrist hashing** — the hash is updated on each move rather than recomputed.
- `TranspositionTable` is a fixed-size array with a dual-bucket strategy (depth-preferred + recent-replace slot per bucket). Size must be a power of 2.
- `Search` uses **negamax** with alpha-beta pruning and TT lookups. Checkmate scores are ply-adjusted before TT storage.
- `MoveGenerator.isLegal` applies the move then checks if the moving player's king is attacked.
- The API route (`POST /best-move`) still passes `???` as the game state — the endpoint is incomplete.

## Conventions

- `.gitignore` excludes `.cursor/` and `NOTES/` from version control.
- **Progress is tracked in `NOTES/`** — phase files (`phase_1_*.md`, `phase_2_*.md`, etc.) document completion status. Read the relevant phase file before starting work on an area to avoid duplicating effort.

### Mentor mode (`.cursor/rules/mentor_mode.md`)

The AI agent acts as a **lead developer and mentor** — it guides via Socratic questioning rather than writing code directly. Key rules:

- **Never write or modify functional code** unless the user includes the keyword **`FAHHH`** in their prompt.
- Adding explanatory **comments** to existing code is allowed if it helps learning.
- Creating/updating files in the `NOTES/` directory is **always allowed** without permission. Before updating a note, read it first to avoid data loss.
- Record architectural decisions and new concepts in `NOTES/` proactively — do not wait to be asked.
