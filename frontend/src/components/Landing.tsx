interface LandingProps {
  loading: boolean
  onStart: () => void
}

export function Landing({ loading, onStart }: LandingProps) {
  return (
    <div className="landing-card">
      <span className="landing-piece">&#9822;</span>
      <div className="landing-text">
        <span className="landing-heading">Play against the engine</span>
        <span className="landing-desc">
          Make a move and the engine will respond using alpha-beta search.
        </span>
      </div>
      <button onClick={onStart} disabled={loading}>
        {loading ? "Starting..." : "New Game"}
      </button>
    </div>
  )
}
