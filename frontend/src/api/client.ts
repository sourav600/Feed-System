import axios, { AxiosError, type InternalAxiosRequestConfig } from "axios";

const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

export const apiClient = axios.create({
  baseURL: BASE_URL,
  withCredentials: true,
});

function readCookie(name: string): string | null {
  const match = document.cookie.match(new RegExp(`(?:^|; )${name}=([^;]*)`));
  return match ? decodeURIComponent(match[1]) : null;
}

const MUTATING_METHODS = new Set(["post", "put", "patch", "delete"]);

// Manual double-submit CSRF header, rather than relying on axios's built-in xsrfCookieName/
// xsrfHeaderName: that auto-attach behavior only kicks in for same-origin requests in recent
// axios versions, and the gateway (different port) counts as cross-origin here.
apiClient.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const method = config.method?.toLowerCase();
  if (method && MUTATING_METHODS.has(method)) {
    const csrfToken = readCookie("XSRF-TOKEN");
    if (csrfToken) {
      config.headers.set("X-XSRF-TOKEN", csrfToken);
    }
  }
  return config;
});

let refreshPromise: Promise<void> | null = null;

function refreshAccessToken(): Promise<void> {
  if (!refreshPromise) {
    refreshPromise = apiClient
      .post("/api/auth/refresh")
      .then(() => undefined)
      .finally(() => {
        refreshPromise = null;
      });
  }
  return refreshPromise;
}

// On a 401 (expired access token), attempt exactly one silent refresh-and-retry. Auth endpoints
// themselves are excluded so a failed login/refresh doesn't recurse into another refresh attempt.
apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const original = error.config as (InternalAxiosRequestConfig & { _retried?: boolean }) | undefined;
    const isAuthEndpoint = original?.url?.startsWith("/api/auth/");

    if (error.response?.status === 401 && original && !original._retried && !isAuthEndpoint) {
      original._retried = true;
      try {
        await refreshAccessToken();
        return apiClient(original);
      } catch {
        // Refresh itself failed - fall through and let the original 401 propagate so callers
        // (ProtectedRoute's current-user query) can redirect to /login.
      }
    }
    return Promise.reject(error);
  },
);
