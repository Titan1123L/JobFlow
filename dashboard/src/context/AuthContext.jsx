import { createContext, useContext, useState } from "react"
import { authApi } from "@/api/auth"

const AuthContext = createContext()

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem("token"))

  async function login(email, password) {
    const data = await authApi.login(email, password)
    localStorage.setItem("token", data.token)
    setToken(data.token)
  }

  async function register(email, password) {
    await authApi.register(email, password)
  }

  function logout() {
    localStorage.removeItem("token")
    setToken(null)
  }

  return (
    <AuthContext.Provider value={{ token, isAuthenticated: !!token, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  return useContext(AuthContext)
}