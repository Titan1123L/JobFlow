import { api } from "./client"

export const apiKeysApi = {
  list: () => api.get("/api/keys"),
  create: (name) => api.post("/api/keys", { name }),
  revoke: (id) => api.delete(`/api/keys/${id}`),
}