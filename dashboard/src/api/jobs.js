import { api } from "./client"

function buildQuery(params) {
  const query = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") {
      query.set(key, value)
    }
  })
  return query.toString()
}

export const jobsApi = {
  list: ({ status, from, to, page = 0, size = 20, sort = "createdAt,desc" } = {}) => {
    const query = buildQuery({ status, from, to, page, size, sort })
    return api.get(`/api/jobs?${query}`)
  },
  get: (id) => api.get(`/api/jobs/${id}`),
  create: (payload) => api.post("/api/jobs", payload),
  cancel: (id) => api.delete(`/api/jobs/${id}`),
  retry: (id) => api.post(`/api/jobs/${id}/retry`, {}),
}