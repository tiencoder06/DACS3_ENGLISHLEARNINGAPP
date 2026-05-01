import React, { useState, useEffect, useMemo } from "react";
import { useVocabulary } from "./useVocabulary";
import { getLessons } from "../lessons/lessonService";
import { type Lesson } from "../../models/Lesson";
import { type Vocabulary, type VocabularyStatus } from "../../models/Vocabulary";
import VocabularyFormModal from "./VocabularyFormModal";
import ImportVocabularyExcel from "./ImportVocabularyExcel";

const VocabularyPage: React.FC = () => {
  const {
    vocabularyItems,
    loading,
    error,
    reloadVocabulary,
    createVocabulary,
    updateVocabulary,
    deleteVocabulary,
    deleteVocabulariesBatch,
    importVocabularyFull
  } = useVocabulary();

  const [lessons, setLessons] = useState<Lesson[]>([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [lessonFilter, setLessonFilter] = useState<string>("all");
  const [statusFilter, setStatusFilter] = useState<VocabularyStatus | "all">("all");

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedVocab, setSelectedVocab] = useState<Vocabulary | null>(null);

  // Bulk select state
  const [selectedIds, setSelectedIds] = useState<string[]>([]);

  const loadLessons = async () => {
    try {
      const data = await getLessons();
      setLessons(data);
    } catch (err) {
      console.error("Failed to load lessons", err);
    }
  };

  useEffect(() => {
    loadLessons();
  }, []);

  const filteredVocabulary = useMemo(() => {
    return vocabularyItems.filter(item => {
      const matchesSearch = item.word.toLowerCase().includes(searchTerm.toLowerCase()) ||
                           item.meaning.toLowerCase().includes(searchTerm.toLowerCase());

      let matchesLesson = true;
      if (lessonFilter === "unassigned") {
          matchesLesson = !item.lessonId;
      } else if (lessonFilter !== "all") {
          matchesLesson = item.lessonId === lessonFilter;
      }

      const matchesStatus = statusFilter === "all" || item.status === statusFilter;
      return matchesSearch && matchesLesson && matchesStatus;
    });
  }, [vocabularyItems, searchTerm, lessonFilter, statusFilter]);

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

  const handleImportExcel = async (rawData: any[]) => {
      await importVocabularyFull(rawData);
      await loadLessons(); // Refresh lessons in case new ones were created
  };

  const toggleSelectAll = () => {
    if (selectedIds.length === filteredVocabulary.length) {
      setSelectedIds([]);
    } else {
      setSelectedIds(filteredVocabulary.map(v => v.vocabId));
    }
  };

  const toggleSelectOne = (id: string) => {
    setSelectedIds(prev =>
      prev.includes(id) ? prev.filter(i => i !== id) : [...prev, id]
    );
  };

  const handleBulkDelete = async () => {
    if (selectedIds.length === 0) return;
    try {
        await deleteVocabulariesBatch(selectedIds);
        setSelectedIds([]);
    } catch (err) {
        alert("Lỗi khi xóa hàng loạt.");
    }
  };

  const renderLessonLabel = (lessonId: string) => {
    if (!lessonId) return (
        <span className="px-2 py-1 bg-red-50 text-red-600 rounded text-[10px] font-bold border border-red-100">
            Chưa gán bài học
        </span>
    );
    const lesson = lessons.find(l => l.lessonId === lessonId);
    if (!lesson) return (
        <span className="px-2 py-1 bg-amber-50 text-amber-600 rounded text-[10px] font-bold border border-amber-100">
            Unknown lesson
        </span>
    );
    return (
        <span className="px-2 py-1 bg-blue-50 text-blue-600 rounded text-[10px] font-bold border border-blue-100">
            {lesson.name}
        </span>
    );
  };

  return (
    <div className="space-y-6 text-left">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
            <h2 className="text-2xl font-bold text-gray-800">Quản lý từ vựng</h2>
            <div className="mt-1 flex items-center gap-2 text-xs font-medium text-gray-500">
                <span className="bg-gray-100 px-2 py-1 rounded-lg">Tổng: <strong>{vocabularyItems.length}</strong> từ</span>
                {filteredVocabulary.length !== vocabularyItems.length && (
                    <span className="bg-blue-50 text-blue-500 px-2 py-1 rounded-lg">Kết quả lọc: <strong>{filteredVocabulary.length}</strong></span>
                )}
            </div>
        </div>

        <div className="flex flex-wrap items-center gap-3">
            {selectedIds.length > 0 && (
                <button
                    onClick={handleBulkDelete}
                    className="px-4 py-2 bg-red-100 text-red-600 rounded-xl font-bold hover:bg-red-200 transition flex items-center gap-2 shadow-sm"
                >
                    🗑️ Xóa ({selectedIds.length})
                </button>
            )}
            <button
                onClick={reloadVocabulary}
                className="p-2 bg-white border border-gray-200 rounded-xl hover:bg-gray-50 transition shadow-sm"
                title="Tải lại"
            >
                🔄
            </button>

            <ImportVocabularyExcel onImport={handleImportExcel} />

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
                className="w-full pl-10 pr-4 py-2 rounded-xl border border-gray-200 focus:ring-2 focus:ring-blue-500 outline-none transition"
            />
        </div>
        <select
            value={lessonFilter}
            onChange={(e) => setLessonFilter(e.target.value)}
            className="px-4 py-2 rounded-xl border border-gray-200 focus:ring-2 focus:ring-blue-500 outline-none bg-white min-w-[200px]"
        >
            <option value="all">Tất cả bài học</option>
            <option value="unassigned">-- Chưa gán bài học --</option>
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

      <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
        {loading ? (
            <div className="p-12 flex justify-center items-center">
                <div className="animate-spin rounded-full h-10 w-10 border-t-2 border-b-2 border-blue-600"></div>
            </div>
        ) : error ? (
            <div className="p-12 text-center text-red-500 italic">{error}</div>
        ) : filteredVocabulary.length === 0 ? (
            <div className="p-12 text-center text-gray-400">Không tìm thấy từ vựng nào.</div>
        ) : (
            <div className="overflow-x-auto">
                <table className="w-full text-left border-collapse">
                    <thead className="bg-gray-50 border-b border-gray-100">
                        <tr>
                            <th className="px-6 py-4 w-10">
                                <input
                                    type="checkbox"
                                    checked={selectedIds.length === filteredVocabulary.length && filteredVocabulary.length > 0}
                                    onChange={toggleSelectAll}
                                    className="w-4 h-4 rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                                />
                            </th>
                            <th className="px-6 py-4 text-xs font-bold text-gray-500 uppercase">Từ vựng</th>
                            <th className="px-6 py-4 text-xs font-bold text-gray-500 uppercase">Ý nghĩa</th>
                            <th className="px-6 py-4 text-xs font-bold text-gray-500 uppercase">Ví dụ</th>
                            <th className="px-6 py-4 text-xs font-bold text-gray-500 uppercase">Bài học</th>
                            <th className="px-6 py-4 text-xs font-bold text-gray-500 uppercase">Phiên âm</th>
                            <th className="px-6 py-4 text-xs font-bold text-gray-500 uppercase text-right">Thao tác</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-100">
                        {filteredVocabulary.map((vocab) => (
                            <tr key={vocab.vocabId} className={`hover:bg-gray-50 transition group ${selectedIds.includes(vocab.vocabId) ? 'bg-blue-50' : ''}`}>
                                <td className="px-6 py-4">
                                    <input
                                        type="checkbox"
                                        checked={selectedIds.includes(vocab.vocabId)}
                                        onChange={() => toggleSelectOne(vocab.vocabId)}
                                        className="w-4 h-4 rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                                    />
                                </td>
                                <td className="px-6 py-4">
                                    <div className="text-sm font-bold text-gray-900">{vocab.word}</div>
                                    <div className="text-[10px] text-gray-400">{(vocab.pronunciationSource || "TTS").toUpperCase()}</div>
                                </td>
                                <td className="px-6 py-4 text-sm text-gray-600">{vocab.meaning}</td>
                                <td className="px-6 py-4 text-sm text-gray-500 max-w-xs truncate">{vocab.exampleSentence || vocab.example || "---"}</td>
                                <td className="px-6 py-4">{renderLessonLabel(vocab.lessonId)}</td>
                                <td className="px-6 py-4 text-sm text-gray-500 italic">{vocab.pronunciation || "---"}</td>
                                <td className="px-6 py-4 text-right space-x-2">
                                    <button onClick={() => handleEdit(vocab)} className="p-2 text-blue-600 hover:bg-blue-50 rounded-lg transition">✏️</button>
                                    <button onClick={() => deleteVocabulary(vocab.vocabId)} className="p-2 text-red-600 hover:bg-red-50 rounded-lg transition">🗑️</button>
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
