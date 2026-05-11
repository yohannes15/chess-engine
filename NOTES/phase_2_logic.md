# Phase 2: Core Chess Logic - Goals

The objective of this phase is to implement the rules of chess: how pieces move, what moves are legal, and how the game state changes.

## Key Concepts to Explore

### 1. Movement Types
- **Sliding Pieces** (Rook, Bishop, Queen): Move in a direction until blocked or a capture occurs.
- **Leaping Pieces** (Knight, King): Move to specific relative squares regardless of intermediate squares.
- **Pawn Logic**: Unique movement (straight), capture (diagonal), and special rules (double move, promotion, en passant).

### 2. Move Representation
- What information must a `Move` object contain?
    - `from`: The starting Square.
    - `to`: The destination Square.
    - `piece`: The piece being moved.
    - `capture`: Optional piece being captured.
    - `promotion`: Optional role for pawn promotion.

### 3. Move Generation
- **Pseudo-legal moves**: Moves that follow piece movement rules but might leave the King in check.
- **Legal moves**: Pseudo-legal moves that do NOT leave the King in check.

## Architectural Decisions

- **Direction Logic**: How do we represent "steps" on a 1D board?
    - North: `+8`
    - South: `-8`
    - East: `+1`
    - West: `-1`
    - Diagonals: `+7, +9, -7, -9`

- **Edge Detection**: How do we know if a move like `+1` from `h4` (index 31) has wrapped around to `a5` (index 32)?

## Study Guide: Key Concepts

### Sliding vs. Leaping
- **Sliding**: Requires a loop to "raycast" in a direction.
- **Leaping**: A simple set of offset checks.

### Pseudo-legal vs. Legal
- In chess engines, we usually generate all "pseudo-legal" moves first, then filter out those that result in the King being captured.
