const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

function getToken() {
  return localStorage.getItem("token");
}

async function request(path, options = {}) {
  const token = getToken();
  const headers = {
    "Content-Type": "application/json",
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...options.headers,
  };

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers,
  });

  const isAuthEndpoint = path.startsWith("/api/auth/");

  if (response.status === 401 && !isAuthEndpoint) {
    localStorage.removeItem("token");
    if (window.location.pathname !== "/login") {
      window.location.href = "/login";
    }
    throw new Error("Session expired");
  }

  if (!response.ok) {
    let errorBody;
    try {
      errorBody = await response.json();
    } catch {
      errorBody = { message: "An unexpected error occurred" };
    }
    throw new Error(errorBody.message || "Request failed");
  }

  if (
    response.status === 202 &&
    response.headers.get("content-length") === "0"
  ) {
    return null;
  }
  const text = await response.text();
  return text ? JSON.parse(text) : null;
}

export const api = {
  get: (path) => request(path),
  post: (path, body) => request(path, { method: "POST", body: JSON.stringify(body) }),
  delete: (path) => request(path, { method: "DELETE" }),
  getBlobUrl: async (path) => {
    const token = getToken()
    const response = await fetch(`${API_BASE_URL}${path}`, {
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    })
    if (!response.ok) throw new Error("Failed to load file")
    const blob = await response.blob()
    return URL.createObjectURL(blob)
  },
}