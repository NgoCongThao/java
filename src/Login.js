import React, { useState } from "react";
import axiosClient from "./api/axiosClient";

function Login() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");

  const submit = async (e) => {
    e.preventDefault();

    try {
      const res = await axiosClient.post("/api/admin/login", {
        username,
        password,
      });

      // backend trả token dạng text
      const token = res.data;

      localStorage.setItem("token", token);

      console.log("LOGIN OK - TOKEN:", token);

      window.location.href = "/";
    } catch (err) {
      console.error("LOGIN ERROR:", err);
      alert("Đăng nhập thất bại");
    }
  };

  return (
    <div className="login-form">
      <h1>Đăng nhập Admin</h1>
      <form onSubmit={submit}>
        <input
          type="text"
          placeholder="Username"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          required
        />
        <input
          type="password"
          placeholder="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
        <button type="submit">Đăng nhập</button>
      </form>
    </div>
  );
}

export default Login;