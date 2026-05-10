# Phase 1: Domain Modeling - Goals

The objective of this phase is to establish the core types and structures that will represent a game of chess.

## Key Questions to Explore

- What are the fundamental "nouns" in chess that need to be represented in our code?
    1. Board (8x8 grid)
    2. Piece:
        - A Color: White OR Black
        - A Role: Pawn, Rook, Knight, Bishop, Queen, King

- How can we represent these nouns in a way that prevents invalid states?
    - **Decision**: Use Scala 3 `enum` for `Color` and `Role`.
    - **Decision**: Use `Option[Piece]` to represent the presence or absence of a piece on a square.

- What are the trade-offs between different ways of representing the 8x8 grid?
    - **Decision**: Use a 1D Immutable `Vector[Option[Piece]]` of size 64.
    - **Decision**: Define `Square` as an **Opaque Type** wrapping an `Int` (0-63).
    - **Reasoning**: 
        - **Purity**: `Vector` is immutable and efficient for updates.
        - **Performance**: Opaque types have zero runtime overhead; Vector has good structural sharing.
        - **Safety**: The compiler prevents treating a random `Int` as a `Square`.
        - **Validation**: Factory methods will ensure only valid squares (0-63) exist.
    - **Move Logic**: Validation will check for path obstruction.
        - If a piece of the **same color** is in the way, the path is blocked.
        - If a piece of the **opposite color** is in the way, it can be captured (but the path beyond it is still blocked).

## Architectural Considerations

### Coordinate Systems vs. Content
- **Square**: The "address" or location on the board (Opaque Int).
    - **Decision**: Use the **0 = a1, 63 = h8** mapping (standard in `scalachess`).
    - **Reasoning**: Ranks and files increase linearly with the index, making coordinate math (like `index / 8` for rank and `index % 8` for file) very intuitive.
- **Piece**: The "content" at a specific Square (Color + Role).
- **Board**: The container that maps Squares to Pieces (Vector).
    - **Decision**: Use a **Smart Constructor** (private constructor + factory methods) for `Board`.
    - **Reasoning**: Ensures that a `Board` can only be created with exactly 64 squares, preventing invalid states.

### File Organization (Standard Practice)
- `Color.scala`, `Role.scala`, `Piece.scala`: Core building blocks.
- `Square.scala`: Coordinate logic.
- `Board.scala`: The state of the board.

## Study Guide: Key Concepts

### Option Type
In functional programming, we avoid `null`. Instead, we use `Option[A]`, which can be:
- `Some(value)`: The value is present.
- `None`: The value is absent.

### Enums (Algebraic Data Types)
Scala 3 `enum` is perfect for defining a fixed set of possibilities.

### Opaque Types (Scala 3)
Opaque types allow you to hide the implementation details of a type while keeping the performance of the underlying type.
- **Benefit**: No wrapping/unwrapping overhead at runtime.
- **Validation**: Enforce constraints (like 0-63) in the factory methods.

### Chess Terminology: Ranks and Files
- **File**: A vertical column on the board (labeled **a-h**).
    - Calculation: `index % 8` (The remainder: "How far into the current row?")
- **Rank**: A horizontal row on the board (labeled **1-8**).
    - Calculation: `index / 8` (The quotient: "How many full rows have I passed?")
- **Example**: Square `a1` is File 0, Rank 0 (Index 0). Square `h8` is File 7, Rank 7 (Index 63).
