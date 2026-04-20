import React, { useState, useEffect } from "react";
import { useVocabulary } from "./useVocabulary";
import { getLessons } from "../lessons/lessonService";
import { type Lesson } from "../../models/Lesson";
import { type Vocabulary, type VocabularyStatus } from "../../models/Vocabulary";
import VocabularyFormModal from "./VocabularyFormModal";

const VocabularyPage: React.FC = () => {
  const {
    vocabularyItems,
    loading,
    error,
    reloadVocabulary,
    createVocabulary,
    updateVocabulary,
    deleteVocabulary
  } = useVocabulary();

  const [lessons, setLessons] = useState<Lesson[]>([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [lessonFilter, setLessonFilter] = useState<string>("all");
  const [statusFilter, setStatusFilter] = useState<VocabularyStatus | "all">("all");

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedVocab, setSelectedVocab] = useState<Vocabulary | null>(null);

  useEffect(() => {
    const loadLessons = async () => {
      try {
        const data = await getLessons();
        setLessons(data);
      } catch (err) {
        console.error("Failed to load lessons", err);
      }
    };
    loadLessons();
  }, []);

  const getLessonName = (lessonId: string) => {
    return lessons.find(l => l.lessonId === lessonId)?.name || "Unknown lesson";
  };

  const filteredVocabulary = vocabularyItems.filter(item => {
    const matchesSearch = item.word.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         item.meaning.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesLesson = lessonFilter === "all" || item.lessonId === lessonFilter;
    const matchesStatus = statusFilter === "all" || item.status === statusFilter;
    return matchesSearch && matchesLesson && matchesStatus;
  });

  const handleAddNew = () => {
    setSelectedVocab(null);
    setIsModalOpen(true);
  };

  const handleEdit = (vocab: Vocabulary) => {
    setSelectedVocab(vocab);
    setIsModalOpen(true);
  };

  const handleFormSubmit = async (data: Partial<Vocabulary>) => {
    if (selectedVocab) {
      await updateVocabulary(selectedVocab.vocabId, data);
    } else {
      await createVocabulary(data);
    }
  };

  return (
    <div className="space-y-6">
      <div className="bg-blue-50 p-2 rounded text-[10px] text-blue-600 font-bold mb-2">
        Real CRUD VocabularyPage loaded
      </div>

      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <h2 className="text-2xl font-bold text-gray-800">Quản lý từ vựng</h2>
        <div className="flex items-center gap-3">
            <button
                onClick={reloadVocabulary}
                className="p-2 bg-white border border-gray-200 rounded-xl hover:bg-gray-50 transition shadow-sm"
                title="Tải lại"
            >
                🔄
            </button>
            <button
                onClick={handleAddNew}
                className="px-6 py-2.5 bg-blue-600 text-white rounded-xl font-bold hover:bg-blue-700 transition shadow-lg shadow-blue-200"
            >
                + Thêm từ vựng mới
            </button>
        </div>
      </div>

      {/* Filters */}
      <div className="bg-white p-4 rounded-2xl shadow-sm border border-gray-100 flex flex-col lg:flex-row gap-4">
        <div className="flex-1 relative">
            <span className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400">🔍</span>
            <input
                type="text"
                placeholder="Tìm kiếm từ hoặc nghĩa..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full pl-10 pr-4 py-2 rounded-xl border border-gray-200 focus:ring-2 focus:ring-blue-500 outline-none transition text-left"
            />
        </div>
        <select
            value={lessonFilter}
            onChange={(e) => setLessonFilter(e.target.value)}
            className="px-4 py-2 rounded-xl border border-gray-200 focus:ring-2 focus:ring-blue-500 outline-none bg-white min-w-[200px]"
        >
            <option value="all">Tất cả bài học</option>
            {lessons.map(l => (
                <option key={l.lessonId} value={l.lessonId}>{l.name}</option>
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

        {!loading && !error && filteredVocabulary.length === 0 && (
            <div className="p-12 text-center text-gray-400">
                Không tìm thấy từ vựng nào.
            </div>
        )}

        {!loading && !error && filteredVocabulary.length > 0 && (
            <div className="overflow-x-auto text-left">
                <table className="w-full text-left border-collapse">
                    <thead className="bg-gray-50 border-b border-gray-100">
                        <tr>
                            <th className="px-6 py-4 text-xs font-bold text-gray-500 uppercase tracking-wider">Từ vựng</th>
                            <th className="px-6 py-4 text-xs font-bold text-gray-500 uppercase tracking-wider">Ý nghĩa</th>
                            <th className="px-6 py-4 text-xs font-bold text-gray-500 uppercase tracking-wider">Bài học</th>
                            <th className="px-6 py-4 text-xs font-bold text-gray-500 uppercase tracking-wider">Phiên âm</th>
                            <th className="px-6 py-4 text-xs font-bold text-gray-500 uppercase tracking-wider">Loại từ</th>
                            <th className="px-6 py-4 text-xs font-bold text-gray-500 uppercase tracking-wider">Trạng thái</th>
                            <th className="px-6 py-4 text-xs font-bold text-gray-500 uppercase tracking-wider text-right">Thao tác</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-100">
                        {filteredVocabulary.map((vocab) => (
                            <tr key={vocab.vocabId} className="hover:bg-gray-50 transition group">
                                <td className="px-6 py-4">
                                    <div className="text-sm font-bold text-gray-900 group-hover:text-blue-600 transition">{vocab.word}</div>
                                    <div className="text-[10px] text-gray-400">{(vocab.pronunciationSource || "UNKNOWN").toUpperCase()}</div>
                                </td>
                                <td className="px-6 py-4 text-sm text-gray-600">
                                    {vocab.meaning}
                                </td>
                                <td className="px-6 py-4">
                                    <span className="px-2 py-1 bg-gray-100 text-gray-600 rounded text-xs font-medium border border-gray-200">
                                        {getLessonName(vocab.lessonId)}
                                    </span>
                                </td>
                                <td className="px-6 py-4 text-sm text-gray-500 italic">
                                    {vocab.pronunciation || "---"}
                                </td>
                                <td className="px-6 py-4 text-sm">
                                    <span className="text-blue-600 font-medium">{vocab.partOfSpeech || "---"}</span>
                                </td>
                                <td className="px-6 py-4 text-sm">
                                    <span className={`px-3 py-1 rounded-full text-xs font-bold ${
                                        vocab.status === "active"
                                        ? "bg-green-100 text-green-600"
                                        : "bg-orange-100 text-orange-600"
                                    }`}>
                                        {vocab.status === "active" ? "Hoạt động" : "Tạm ngưng"}
                                    </span>
                                </td>
                                <td className="px-6 py-4 text-right space-x-2">
                                    <button
                                        onClick={() => handleEdit(vocab)}
                                        className="p-2 text-blue-600 hover:bg-blue-50 rounded-lg transition"
                                        title="Chỉnh sửa"
                                    >
                                        ✏️
                                    </button>
                                    <button
                                        onClick={() => deleteVocabulary(vocab.vocabId)}
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

      {isModalOpen && (
        <VocabularyFormModal
          isOpen={isModalOpen}
          onClose={() => setIsModalOpen(false)}
          onSubmit={handleFormSubmit}
          initialData={selectedVocab}
        />
      )}
    </div>
  );
};

export default VocabularyPage;
