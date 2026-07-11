import { type FormEvent, useState } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";
import { useCurrentUser, useLogin } from "../hooks/useAuth";
import type { ApiError } from "../api/types";
import { isAxiosError } from "axios";

export function LoginPage() {
  const { data: user, isLoading } = useCurrentUser();
  const loginMutation = useLogin();
  const navigate = useNavigate();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  if (!isLoading && user) {
    return <Navigate to="/" replace />;
  }

  function handleSubmit(event: FormEvent) {
    event.preventDefault();
    loginMutation.mutate(
      { email, password },
      { onSuccess: () => navigate("/", { replace: true }) },
    );
  }

  const errorMessage = extractErrorMessage(loginMutation.error);

  return (
    <section className="_social_login_wrapper _layout_main_wrapper">
      <div className="_shape_one">
        <img src="/assets/images/shape1.svg" alt="" className="_shape_img" />
      </div>
      <div className="_shape_two">
        <img src="/assets/images/shape2.svg" alt="" className="_shape_img" />
      </div>
      <div className="_shape_three">
        <img src="/assets/images/shape3.svg" alt="" className="_shape_img" />
      </div>
      <div className="_social_login_wrap">
        <div className="container">
          <div className="row align-items-center">
            <div className="col-xl-8 col-lg-8 col-md-12 col-sm-12">
              <div className="_social_login_left">
                <div className="_social_login_left_image">
                  <img src="/assets/images/login.png" alt="" className="_left_img" />
                </div>
              </div>
            </div>
            <div className="col-xl-4 col-lg-4 col-md-12 col-sm-12">
              <div className="_social_login_content">
                <div className="_social_login_left_logo _mar_b28">
                  <img src="/assets/images/logo.svg" alt="News Feed" className="_left_logo" />
                </div>
                <p className="_social_login_content_para _mar_b8">Welcome back</p>
                <h4 className="_social_login_content_title _titl4 _mar_b50">Login to your account</h4>

                <form className="_social_login_form" onSubmit={handleSubmit}>
                  <div className="row">
                    <div className="col-xl-12 col-lg-12 col-md-12 col-sm-12">
                      <div className="_social_login_form_input _mar_b14">
                        <label className="_social_login_label _mar_b8">Email</label>
                        <input
                          type="email"
                          className="form-control _social_login_input"
                          value={email}
                          onChange={(e) => setEmail(e.target.value)}
                          required
                        />
                      </div>
                    </div>
                    <div className="col-xl-12 col-lg-12 col-md-12 col-sm-12">
                      <div className="_social_login_form_input _mar_b14">
                        <label className="_social_login_label _mar_b8">Password</label>
                        <input
                          type="password"
                          className="form-control _social_login_input"
                          value={password}
                          onChange={(e) => setPassword(e.target.value)}
                          required
                        />
                      </div>
                    </div>
                  </div>

                  {errorMessage && (
                    <div className="row">
                      <div className="col-12">
                        <p role="alert" style={{ color: "#e53e3e", marginBottom: 12 }}>
                          {errorMessage}
                        </p>
                      </div>
                    </div>
                  )}

                  <div className="row">
                    <div className="col-lg-12 col-md-12 col-xl-12 col-sm-12">
                      <div className="_social_login_form_btn _mar_t40 _mar_b60">
                        <button type="submit" className="_social_login_form_btn_link _btn1" disabled={loginMutation.isPending}>
                          {loginMutation.isPending ? "Logging in..." : "Login now"}
                        </button>
                      </div>
                    </div>
                  </div>
                </form>

                <div className="row">
                  <div className="col-xl-12 col-lg-12 col-md-12 col-sm-12">
                    <div className="_social_login_bottom_txt">
                      <p className="_social_login_bottom_txt_para">
                        Dont have an account? <Link to="/register">Create New Account</Link>
                      </p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}

function extractErrorMessage(error: unknown): string | null {
  if (!error) return null;
  if (isAxiosError<ApiError>(error)) {
    return error.response?.data?.message ?? "Something went wrong. Please try again.";
  }
  return "Something went wrong. Please try again.";
}
