import { PlayerVsEngine } from "./modes/PlayerVsEngine"

function App() {
  return (
    <div className="shell">
      <header className="app-header">
        <span className="app-title">Chess Engine</span>
      </header>

      <PlayerVsEngine />
    </div>
  )
}

export default App
