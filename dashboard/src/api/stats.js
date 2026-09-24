import { api } from "./client"

export const statsApi = {
  get: () => api.get("/api/stats"),
}