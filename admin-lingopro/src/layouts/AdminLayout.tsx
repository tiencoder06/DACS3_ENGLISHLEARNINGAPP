import React from "react";
import { Link, Outlet, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

const sidebarLinks = [
  { name: "Dashboard", path: "/dashboard", icon: "📊" },
  { name: "Topics", path: "/topics", icon: "📚" },
  { name: "Lessons", path: "/lessons", icon: "📖" },
  { name: "Vocabulary", path: "/vocabulary", icon: "🔤" },
  { name: "Questions", path: "/questions", icon: "❓" },
  { name: "Import Data", path: "/import", icon: "📥" },
  { name: "Users", path: "/users", icon: "👥" },
  { name: "Results", path: "/results", icon: "🏆" },
];

const AdminLayout: React.FC = () => {
  const { logout, userProfile } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();

  const handleLogout = async () => {
    try {
      await logout();
      navigate("/login");
    } catch (error) {
      console.error("Failed to logout", error);
    }
  };

  return (
    <div className="flex h-screen bg-gray-50">
      {/* Sidebar */}
      <aside className="w-64 bg-white border-r border-gray-200 flex flex-col fixed h-full">
        <div className="p-6 border-b border-gray-100">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-blue-600 rounded-xl flex items-center justify-center shadow-lg shadow-blue-100">
              <span className="text-white text-xl font-bold">L</span>
            </div>
            <span className="text-xl font-bold text-gray-900 tracking-tight">LingoPro</span>
          </div>
        </div>

        <nav className="flex-1 overflow-y-auto p-4 space-y-1">
          {sidebarLinks.map((link) => {
            const isActive = location.pathname === link.path;
            return (
              <Link
                key={link.path}
                to={link.path}
                className={`flex items-center gap-3 px-4 py-3 rounded-xl transition-all duration-200 ${
                  isActive
                    ? "bg-blue-50 text-blue-600 font-semibold"
                    : "text-gray-500 hover:bg-gray-100 hover:text-gray-900"
                }`}
              >
                <span className="text-xl">{link.icon}</span>
                <span>{link.name}</span>
              </Link>
            );
          })}
        </nav>

        <div className="p-4 border-t border-gray-100">
          <button
            onClick={handleLogout}
            className="flex items-center gap-3 w-full px-4 py-3 text-red-500 hover:bg-red-50 rounded-xl transition-all duration-200"
          >
            <span className="text-xl">🚪</span>
            <span className="font-semibold">Đăng xuất</span>
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <div className="flex-1 ml-64 flex flex-col">
        {/* Topbar */}
        <header className="h-20 bg-white border-b border-gray-200 flex items-center justify-between px-8 sticky top-0 z-10">
          <h2 className="text-xl font-semibold text-gray-800">
            {sidebarLinks.find(l => l.path === location.pathname)?.name || "Admin Panel"}
          </h2>

          <div className="flex items-center gap-4">
            <div className="text-right">
              <p className="text-sm font-bold text-gray-900">{userProfile?.fullName || "Admin"}</p>
              <p className="text-xs text-gray-500">{userProfile?.email}</p>
            </div>
            <div className="w-10 h-10 rounded-full bg-blue-100 border-2 border-blue-500 flex items-center justify-center overflow-hidden">
                <span className="text-blue-600 font-bold">{userProfile?.fullName?.[0]?.toUpperCase() || "A"}</span>
            </div>
          </div>
        </header>

        {/* Page Area */}
        <main className="flex-1 p-8 overflow-y-auto">
          <Outlet />
        </main>
      </div>
    </div>
  );
};

export default AdminLayout;
