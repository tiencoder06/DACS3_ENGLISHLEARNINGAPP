import React, { useState, useEffect } from "react";
import { useLessons } from "./useLessons";
import { getTopics } from "../topics/topicService";
import { type Topic } from "../../models/Topic";
import { type Lesson, type LessonStatus } from "../../models/Lesson";
import LessonFormModal from "./LessonFormModal";

const LessonsPage: React.FC = () => {
  const {
    lessons,
    loading,
    error,
    reloadLessons,
    createLesson,
    updateLesson,
    deleteLesson
  } = useLessons();

  const [topics, setTopics] = useState<Topic[]>([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [topicFilter, setTopicFilter] = useState<string>("all");
  const [statusFilter, setStatusFilter] = useState<LessonStatus | "all">("all");

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedLesson, setSelectedTopic] = useState<Lesson | null>(null);

  useEffect(() => {
    const loadTopics = async () => {
      try {
        const data = await getTopics();
        setTopics(data);
      } catch (err) {
        console.error("Failed to load topics", err);
      }
    };
    loadTopics();
  }, []);

  const getTopicName = (topicId: string) => {
    return topics.find(t => t.topicId === topicId)?.name || "Unknown topic";
  };

  const filteredLessons = lessons.filter(lesson => {
    const matchesSearch = lesson.name.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesTopic = topicFilter === "all" || lesson.topicId === topicFilter;
    const matchesStatus = statusFilter === "all" || lesson.status === statusFilter;
    return matchesSearch && matchesTopic && matchesStatus;
  });

  const handleAddNew = () => {
    setSelectedTopic(null);
    setIsModalOpen(true);
  };

  const handleEdit = (lesson: Lesson) => {
    setSelectedTopic(lesson);
    setIsModalOpen(true);
  };

  const handleFormSubmit = async (data: Partial<Lesson>) => {
    if (selectedLesson) {
      await updateLesson(selectedLesson.lessonId, data);
    } else {
      await createLesson(data);
    }
  };

  return (
    <div className="space-y-6">
      <div className="bg-blue-50 p-2 rounded text-[10px] text-blue-600 font-bold mb-2">
        Real CRUD LessonsPage loaded
      </div>

      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <h2 className="text-2xl font-bold text-gray-800">Quản lý bài học</h2>
        <div className="flex items-center gap-3">
            <button
                onClick={reloadLessons}
                className="p-2 bg-white border border-gray-200 rounded-xl hover:bg-gray-50 transition shadow-sm"
                title="Tải lại"
            >
                🔄
            </button>
            <button
                onClick={handleAddNew}
                className="px-6 py-2.5 bg-blue-600 text-white rounded-xl font-bold hover:bg-blue-700 transition shadow-lg shadow-blue-200"
            >
                + Thêm bài học mới
            </button>
        </div>
      </div>

      {/* Filters */}
      <div className="bg-white p-4 rounded-2xl shadow-sm border border-gray-100 flex flex-col lg:flex-row gap-4">
        <div className="flex-1 relative">
            <span className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400">🔍</span>
            <input
                type="text"
                placeholder="Tìm kiếm theo tên bài học..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full pl-10 pr-4 py-2 rounded-xl border border-gray-200 focus:ring-2 focus:ring-blue-500 outline-none transition"
            />
        </div>
        <select
            value={topicFilter}
            onChange={(e) => setTopicFilter(e.target.value)}
            className="px-4 py-2 rounded-xl border border-gray-200 focus:ring-2 focus:ring-blue-500 outline-none bg-white min-w-[200px]"
        >
            <option value="all">Tất cả chủ đề</option>
            {topics.map(t => (
                <option key={t.topicId} value={t.topicId}>{t.name}</option>
            ))}
        </select>
        <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value as any)}
            className="px-4 py-2 rounded-xl border border-gray-200 focus:ring-2 focus:ring-blue-500 outline-none bg-white min-w-[150px]"
        >
            <option value="all">Tất cả trạng thái</option>
            <option value="active">Hoạt động</option>
            <option value="inactive">Tạm ngưng</option>
        </select>
      </div>

      {/* Table */}
      <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
        {loading && (
            <div className="p-12 flex justify-center">
                <div className="animate-spin rounded-full h-10 w-10 border-t-2 border-b-2 border-blue-600"></div>
            </div>
        )}

        {error && (
            <div className="p-12 text-center text-red-500 italic">
                {error}
            </div>
        )}

        {!loading && !error && filteredLessons.length === 0 && (
            <div className="p-12 text-center text-gray-400">
                Không tìm thấy bài học nào.
            </div>
        )}

        {!loading && !error && filteredLessons.length > 0 && (
            <div className="overflow-x-auto">
                <table className="w-full text-left border-collapse">
                    <thead className="bg-gray-50 border-b border-gray-100">
                        <tr>
                            <th className="px-6 py-4 text-xs font-bold text-gray-500 uppercase tracking-wider">Thứ tự</th>
                            <th className="px-6 py-4 text-xs font-bold text-gray-500 uppercase tracking-wider">Tên bài học</th>
                            <th className="px-6 py-4 text-xs font-bold text-gray-500 uppercase tracking-wider">Chủ đề</th>
                            <th className="px-6 py-4 text-xs font-bold text-gray-500 uppercase tracking-wider">Số từ</th>
                            <th className="px-6 py-4 text-xs font-bold text-gray-500 uppercase tracking-wider">Trạng thái</th>
                            <th className="px-6 py-4 text-xs font-bold text-gray-500 uppercase tracking-wider">Ngày tạo</th>
                            <th className="px-6 py-4 text-xs font-bold text-gray-500 uppercase tracking-wider text-right">Thao tác</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-100">
                        {filteredLessons.map((lesson) => (
                            <tr key={lesson.lessonId} className="hover:bg-gray-50 transition group">
                                <td className="px-6 py-4 text-sm text-gray-600 font-medium">#{lesson.order}</td>
                                <td className="px-6 py-4">
                                    <div className="text-sm font-bold text-gray-900 group-hover:text-blue-600 transition">{lesson.name}</div>
                                    <div className="text-xs text-gray-500 truncate max-w-xs">{lesson.description}</div>
                                </td>
                                <td className="px-6 py-4">
                                    <span className="px-2 py-1 bg-gray-100 text-gray-600 rounded text-xs font-medium border border-gray-200">
                                        {getTopicName(lesson.topicId)}
                                    </span>
                                </td>
                                <td className="px-6 py-4 text-sm text-gray-600">
                                    {lesson.totalWords} từ
                                </td>
                                <td className="px-6 py-4 text-sm">
                                    <span className={`px-3 py-1 rounded-full text-xs font-bold ${
                                        lesson.status === "active"
                                        ? "bg-green-100 text-green-600"
                                        : "bg-orange-100 text-orange-600"
                                    }`}>
                                        {lesson.status === "active" ? "Hoạt động" : "Tạm ngưng"}
                                    </span>
                                </td>
                                <td className="px-6 py-4 text-sm text-gray-500">
                                    {lesson.createdAt?.toDate().toLocaleDateString("vi-VN")}
                                </td>
                                <td className="px-6 py-4 text-right space-x-2">
                                    <button
                                        onClick={() => handleEdit(lesson)}
                                        className="p-2 text-blue-600 hover:bg-blue-50 rounded-lg transition"
                                        title="Chỉnh sửa"
                                    >
                                        ✏️
                                    </button>
                                    <button
                                        onClick={() => deleteLesson(lesson.lessonId)}
                                        className="p-2 text-red-600 hover:bg-red-50 rounded-lg transition"
                                        title="Xóa"
                                    >
                                        🗑️
                                    </button>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        )}
      </div>

      <LessonFormModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSubmit={handleFormSubmit}
        initialData={selectedLesson}
      />
    </div>
  );
};

export default LessonsPage;
