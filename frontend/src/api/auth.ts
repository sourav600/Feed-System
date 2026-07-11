import { apiClient } from "./client";
import type { CurrentUser } from "./types";

export interface RegisterPayload {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
}

export interface LoginPayload {
  email: string;
  password: string;
}

export async function register(payload: RegisterPayload): Promise<CurrentUser> {
  const { data } = await apiClient.post<CurrentUser>("/api/auth/register", payload);
  return data;
}

export async function login(payload: LoginPayload): Promise<CurrentUser> {
  const { data } = await apiClient.post<CurrentUser>("/api/auth/login", payload);
  return data;
}

export async function logout(): Promise<void> {
  await apiClient.post("/api/auth/logout");
}

export async function fetchCurrentUser(): Promise<CurrentUser> {
  const { data } = await apiClient.get<CurrentUser>("/api/users/me");
  return data;
}
