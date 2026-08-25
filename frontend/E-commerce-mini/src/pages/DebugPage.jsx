import { useState } from "react";
import axiosClient from "../axiosClient";

export default function DebugPage() {
  const [username, setUsername] = useState("testuser");
  const [password, setPassword] = useState("password123");
  const [response, setResponse] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  const handleTestLogin = async () => {
    setLoading(true);
    setError(null);
    setResponse(null);
    
    try {
      console.log("🔵 Testing login with:", { username, password });
      
      const res = await axiosClient.post("/auth/login", {
        username: username.trim(),
        password: password.trim(),
      });
      
      console.log("✅ Full Response:", res);
      console.log("✅ Result:", res.result);
      console.log("✅ Token:", res.result?.token);
      console.log("✅ User:", res.result?.user);
      
      setResponse(res);
    } catch (err) {
      console.error("❌ Error:", err);
      console.error("❌ Status:", err.response?.status);
      console.error("❌ Data:", err.response?.data);
      console.error("❌ Message:", err.message);
      
      setError({
        status: err.response?.status,
        data: err.response?.data,
        message: err.message,
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-100 p-8">
      <div className="max-w-2xl mx-auto">
        <div className="bg-white rounded-lg shadow-lg p-6">
          <h1 className="text-2xl font-bold mb-6">🔧 Debug Login API</h1>
          
          <div className="space-y-4 mb-6">
            <div>
              <label className="block text-sm font-semibold mb-2">Username</label>
              <input
                type="text"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
              />
            </div>
            
            <div>
              <label className="block text-sm font-semibold mb-2">Password</label>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
              />
            </div>
            
            <button
              onClick={handleTestLogin}
              disabled={loading}
              className="w-full bg-indigo-600 hover:bg-indigo-700 text-white font-bold py-2 px-4 rounded-lg disabled:opacity-50"
            >
              {loading ? "⏳ Testing..." : "🧪 Test Login"}
            </button>
          </div>

          {error && (
            <div className="bg-red-50 border border-red-200 rounded-lg p-4 mb-6">
              <h3 className="font-bold text-red-700 mb-2">❌ Error:</h3>
              <pre className="text-sm text-red-600 overflow-auto bg-red-100 p-3 rounded">
                {JSON.stringify(error, null, 2)}
              </pre>
            </div>
          )}

          {response && (
            <div className="bg-green-50 border border-green-200 rounded-lg p-4">
              <h3 className="font-bold text-green-700 mb-2">✅ Success Response:</h3>
              <pre className="text-sm text-green-600 overflow-auto bg-green-100 p-3 rounded">
                {JSON.stringify(response, null, 2)}
              </pre>
            </div>
          )}

          <div className="mt-6 p-4 bg-blue-50 border border-blue-200 rounded-lg text-sm text-blue-700">
            <p className="font-semibold mb-2">📌 Hướng dẫn debug:</p>
            <ol className="list-decimal list-inside space-y-1">
              <li>Nhập username/password giống Postman</li>
              <li>Nhấn "Test Login"</li>
              <li>Check DevTools Console (F12) để xem log chi tiết</li>
              <li>Copy error hoặc response gửi cho tôi</li>
            </ol>
          </div>
        </div>
      </div>
    </div>
  );
}
