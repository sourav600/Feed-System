import { Navigate, Outlet } from "react-router-dom";
import { useCurrentUser } from "../hooks/useAuth";

export function ProtectedRoute() {
  const { data: user, isLoading, isError } = useCurrentUser();

  if (isLoading) {
    return <div className="_page_loading">Loading...</div>;
  }

  if (isError || !user) {
    return <Navigate to="/login" replace />;
  }

  return <Outlet />;
}
