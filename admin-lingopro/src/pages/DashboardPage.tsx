import React, { useEffect, useState } from "react";
import { getDashboardStats, type DashboardStats } from "../features/dashboard/dashboardService";

const DashboardPage: React.FC = () => {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchStats = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await getDashboardStats();
      setStats(data);
    } catch (err: any) {
      setError("Không thể tải dữ liệu thống kê. Vui lòng thử lại.");
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchStats();
  }, []);

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-50 p-6 rounded-2xl border border-red-100 text-center">
        <p className="text-red-600 mb-4">{error}</p>
        <button
          onClick={fetchStats}
          className="px-6 py-2 bg-red-600 text-white rounded-xl hover:bg-red-700 transition"
        >
          Thử lại
        </button>
      </div>
    );
  }

  const statCards = [
    { label: "Tổng số người dùng", value: stats?.totalUsers, icon: "👥", color: "text-blue-600" },
    { label: "Tổng số chủ đề", value: stats?.totalTopics, icon: "📚", color: "text-green-600" },
    { label: "Tổng số bài học", value: stats?.totalLessons, icon: "📖", color: "text-purple-600" },
    { label: "Tổng số từ vựng", value: stats?.totalVocabulary, icon: "🔤", color: "text-yellow-600" },
    { label: "Tổng số câu hỏi", value: stats?.totalQuestions, icon: "❓", color: "text-orange-600" },
    { label: "Kết quả làm bài", value: stats?.totalQuizResults, icon: "🏆", color: "text-indigo-600" },
  ];

  return (
    <div className="space-y-8">
      <div className="flex justify-between items-center">
        <h2 className="text-2xl font-bold text-gray-800">Thống kê hệ thống</h2>
        <button
          onClick={fetchStats}
          className="flex items-center gap-2 px-4 py-2 bg-white border border-gray-200 rounded-xl hover:bg-gray-50 transition shadow-sm text-sm font-medium"
        >
          <span>🔄</span> Làm mới
        </button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {statCards.map((card, index) => (
          <div key={index} className="bg-white p-6 rounded-2xl shadow-sm border border-gray-100 flex items-center gap-4 transition hover:shadow-md">
            <div className="text-4xl">{card.icon}</div>
            <div>
              <p className="text-gray-500 text-sm font-medium">{card.label}</p>
              <h3 className={`text-3xl font-bold mt-1 ${card.color}`}>{card.value?.toLocaleString() || 0}</h3>
            </div>
          </div>
        ))}
      </div>

      <div className="bg-white p-8 rounded-2xl shadow-sm border border-gray-100 min-h-[300px] flex items-center justify-center">
        <div className="text-center">
          <div className="text-5xl mb-4">📈</div>
          <h2 className="text-xl font-semibold text-gray-800">Phân tích xu hướng</h2>
          <p className="text-gray-500 mt-2">Biểu đồ sẽ được cập nhật ở Phase sau.</p>
        </div>
      </div>
    </div>
  );
};

export default DashboardPage;
