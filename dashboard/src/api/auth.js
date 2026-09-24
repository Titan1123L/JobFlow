import { api } from "./client"

export const authApi = {
  register: (email, password) => api.post("/api/auth/register", { email, password }),
  login: (email, password) => api.post("/api/auth/login", { email, password }),
  requestPasswordReset: (email) => api.post("/api/auth/password-reset/request", { email }),
  confirmPasswordReset: (token, newPassword) =>
    api.post("/api/auth/password-reset/confirm", { token, newPassword }),
}