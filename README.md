# Chess Engine

Building a fully functional and pure chess library and backend in Scala 3, Cats Effect, and Http4s, inspired by `scalachess`.

## Project Overview

This project aims to build a robust, high-performance chess library and a scalable real-time multiplayer backend. The focus is on functional purity, type safety, and efficient move generation.

## Phase 1: Domain Modeling & Foundation

- **ADTs**: Define `Piece`, `Color`, `Role`, `Square`, `File`, `Rank`, and `Move`.
- **Board Representation**: Initial implementation using a simple Map or Array.
- **Game State**: Track active color, castling rights, en passant square, and half-move clock.

## Phase 2: Core Chess Logic (The "Library" Part)

- **Move Generation**: Generate pseudo-legal and legal moves.
- **Validation**: Strict validation for checks, pins, and special moves (castling, en passant, promotion).
- **Notation**: Parsers and encoders for **FEN** and **PGN**.
- **Performance**: Transition to **Bitboards** for high-speed move generation and evaluation.

## Phase 3: Testing & Quality Assurance

- **Property-Based Testing**: Use `MUnit` and `ScalaCheck` to verify move generation against known perft results.
- **Perft Testing**: Benchmark move generation speed and correctness.
- **CI/CD**: Automated testing pipeline.

## Phase 4: Engine Implementation (AI)

- **Search**: Minimax with Alpha-Beta pruning, Quiescence search.
- **Evaluation**: Material weights, piece-square tables, and pawn structure analysis.
- **Optimizations**: **Zobrist Hashing**, Transposition Tables, and Move Ordering (Killer moves, Iterative Deepening).

## Phase 5: Stateless HTTP API

- **Endpoints**: Move validation, FEN/PGN parsing, and "best move" suggestions.
- **Stack**: Http4s, Circe for JSON, and Cats Effect for concurrency.

## Phase 6: Real-Time Multiplayer Backend

- State Management: Concurrent game handling using Cats Effect `Ref` or `Deferred`.
- WebSockets: Real-time move broadcasting and player synchronization.
- Game Clock: Implement Fischer/Bronstein increments and time-out handling.
- Persistence: Save game history and state to a database (e.g., PostgreSQL).

## Phase 7: Frontend Integration

- **Scala.js / AI-Assisted UI**: Develop a web interface to interact with the backend.
- **Integration**: Connect the WebSocket and REST API to a visual board.
- **User Experience**: Real-time board updates, move animations, and game status indicators.

## Technical Notes

- **Purity**: Side effects are managed via `IO`.
- **Performance**: Bitboards are essential for the engine's depth.
- **Concurrency**: Leveraging FS2 for streaming WebSocket updates.
