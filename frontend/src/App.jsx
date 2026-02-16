import { useEffect, useState } from 'react'

function App()
{
  const [message, setMessage] = useState("Loading...")

  useEffect(() =>
  {
    fetch("/api/ping")
      .then(res => res.json())
      .then(data => setMessage(data.message))
      .catch(() => setMessage("Error connecting to backend"))
  }, [])

  return (
    <div>
      <h1>Backend says:</h1>
      <p>{message}</p>
    </div>
  )
}

export default App