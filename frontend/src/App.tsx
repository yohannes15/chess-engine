import { PlayerVsEngine } from "./modes/PlayerVsEngine"

function App() {
  return (
    <div className="shell">
      <header className="app-header">
        <span className="app-title">Chess Engine</span>
        <span className="app-subtitle">Negamax · Alpha-Beta · Scala 3</span>
      </header>

      <PlayerVsEngine />
    </div>
  )
}

export default App
