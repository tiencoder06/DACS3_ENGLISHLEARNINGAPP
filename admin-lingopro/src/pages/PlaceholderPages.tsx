import React from "react";

const PageContainer: React.FC<{ title: string }> = ({ title }) => (
  <div className="bg-white p-6 rounded-2xl shadow-sm border border-gray-100 min-h-[500px]">
    <h2 className="text-xl font-bold text-gray-800 mb-6">{title}</h2>
    <div className="flex flex-col items-center justify-center h-64 text-gray-400">
      <div className="text-6xl mb-4">🚧</div>
      <p className="text-lg italic">Tính năng đang được phát triển...</p>
    </div>
  </div>
);

export const LessonsPage = () => <PageContainer title="Quản lý Bài học (Lessons)" />;
export const VocabularyPage = () => <PageContainer title="Quản lý Từ vựng (Vocabulary)" />;
export const QuestionsPage = () => <PageContainer title="Quản lý Câu hỏi (Questions)" />;
export const ImportPage = () => <PageContainer title="Nhập dữ liệu (Import Data)" />;
export const UsersPage = () => <PageContainer title="Quản lý Người dùng (Users)" />;
export const ResultsPage = () => <PageContainer title="Kết quả học tập (Results)" />;
