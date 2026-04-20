import React from "react";

const TopicsPage: React.FC = () => {
  return (
    <div className="bg-white p-6 rounded-2xl shadow-sm border border-gray-100 min-h-[500px]">
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-xl font-bold text-gray-800">Quản lý Chủ đề (Topics)</h2>
        <button className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition">
          + Thêm chủ đề mới
        </button>
      </div>
      <p className="text-gray-500">Chức năng CRUD sẽ được triển khai ở Phase 2.</p>
    </div>
  );
};

export default TopicsPage;
