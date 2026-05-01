import React, { useState } from "react";
import { useTopics } from "./useTopics";
import TopicFormModal from "./TopicFormModal";
import { type Topic, type TopicStatus } from "../../models/Topic";

const TopicsPage: React.FC = () => {
  console.log("[TopicsPage] rendered");

  const {
    topics,
    loading,
    error,
    reload,
    createTopic,
    updateTopic,
    deleteTopic,
    deleteTopicsBatch
  } = useTopics();

  const [searchTerm, setSearchTerm] = useState("");
  const [statusFilter, setStatusFilter] = useState<TopicStatus | "all">("all");

  // State quản lý Modal
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [modalMode, setModalMode] = useState<"create" | "edit">("create");
  const [selectedTopic, setSelectedTopic] = useState<Topic | null>(null);
  const [localError, setLocalError] = useState<string | null>(null);

  // Bulk select state
  const [selectedIds, setSelectedIds] = useState<string[]>([]);

  const filteredTopics = topics.filter(topic => {
    const matchesSearch = topic.name.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesStatus = statusFilter === "all" || topic.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  const handleAddNew = () => {
    setModalMode("create");
    setSelectedTopic(null);
    setIsModalOpen(true);
  };

  const handleEdit = (topic: Topic) => {
    setModalMode("edit");
    setSelectedTopic(topic);
    setIsModalOpen(true);
  };

  const handleFormSubmit = async (data: Partial<Topic>) => {
    setLocalError(null);
    try {
      if (modalMode === "edit" && selectedTopic) {
        await updateTopic(selectedTopic.topicId, data);
      } else {
        await createTopic(data);
      }
      setIsModalOpen(false);
      reload();
    } catch (err: any) {
      setLocalError(err.message || "Lỗi khi lưu dữ liệu");
    }
  };

  const toggleSelectAll = () => {
    if (selectedIds.length === filteredTopics.length) {
      setSelectedIds([]);
    } else {
      setSelectedIds(filteredTopics.map(t => t.topicId));
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
        await deleteTopicsBatch(selectedIds);
        setSelectedIds([]);
    } catch (err) {
        alert("Lỗi khi xóa hàng loạt.");
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
            <h2 className="text-2xl font-bold text-gray-800">Quản lý chủ đề</h2>
        </div>

        <div className="flex flex-wrap items-center gap-3">
            {selectedIds.length > 0 && (
                <button
                    onClick={handleBulkDelete}
                    className="px-4 py-2 bg-red-100 text-red-600 rounded-xl font-bold hover:bg-red-200 transition flex items-center gap-2"
                >
                    🗑️ Xóa đã chọn ({selectedIds.length})
                </button>
            )}
            <button
                onClick={reload}
                className="p-2 bg-white border border-gray-200 rounded-xl hover:bg-gray-50 transition shadow-sm"
                title="Tải lại"
            >
                🔄
            </button>
            <button
                onClick={handleAddNew}
                className="px-6 py-2.5 bg-blue-600 text-white rounded-xl font-bold hover:bg-blue-700 transition shadow-lg shadow-blue-200"
            >
                + Thêm chủ đề mới
            </button>
        </div>
      </div>

      {localError && (
        <div className="bg-red-50 text-red-600 p-4 rounded-xl border border-red-100 flex justify-between">
            <span>{localError}</span>
            <button onClick={() => setLocalError(null)}>×</button>
        </div>
      )}

      {/* Bộ lọc */}
      <div className="bg-white p-4 rounded-2xl shadow-sm border border-gray-100 flex flex-col md:flex-row gap-4">
        <div className="flex-1 relative">
            <span className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400">🔍</span>
            <input
                type="text"
                placeholder="Tìm kiếm theo tên chủ đề..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full pl-10 pr-4 py-2 rounded-xl border border-gray-200 focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none transition"
            />
        </div>
        <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value as any)}
            className="px-4 py-2 rounded-xl border border-gray-200 focus:ring-2 focus:ring-blue-500 outline-none transition bg-white cursor-pointer"
        >
            <option value="all">Tất cả trạng thái</option>
            <option value="active">Hoạt động</option>
            <option value="inactive">Tạm ngưng</option>
        </select>
      </div>

      {/* Danh sách bảng */}
      <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden min-h-[400px]">
        {loading ? (
            <div className="p-24 flex flex-col items-center justify-center">
                <div className="animate-spin rounded-full h-10 w-10 border-t-2 border-b-2 border-blue-600 mb-4"></div>
                <p className="text-gray-400">Đang tải dữ liệu...</p>
            </div>
        ) : error ? (
            <div className="p-24 text-center">
                <p className="text-red-500 mb-4">{error}</p>
                <button onClick={reload} className="text-blue-600 hover:underline">Thử lại</button>
            </div>
        ) : filteredTopics.length === 0 ? (
            <div className="p-24 text-center text-gray-400 italic">
                Chưa có chủ đề nào được tạo.
            </div>
        ) : (
            <div className="overflow-x-auto">
                <table className="w-full text-left border-collapse">
                    <thead className="bg-gray-50 border-b border-gray-100">
                        <tr>
                            <th className="px-6 py-4 w-10">
                                <input
                                    type="checkbox"
                                    checked={selectedIds.length === filteredTopics.length && filteredTopics.length > 0}
                                    onChange={toggleSelectAll}
                                    className="w-4 h-4 rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                                />
                            </th>
                            <th className="px-6 py-4 text-xs font-bold text-gray-500 uppercase tracking-wider">Thứ tự</th>
                            <th className="px-6 py-4 text-xs font-bold text-gray-500 uppercase tracking-wider">Tên chủ đề</th>
                            <th className="px-6 py-4 text-xs font-bold text-gray-500 uppercase tracking-wider">Trạng thái</th>
                            <th className="px-6 py-4 text-xs font-bold text-gray-500 uppercase tracking-wider text-right">Thao tác</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-100">
                        {filteredTopics.map((topic) => (
                            <tr key={topic.topicId} className={`hover:bg-gray-50 transition group ${selectedIds.includes(topic.topicId) ? 'bg-blue-50' : ''}`}>
                                <td className="px-6 py-4">
                                    <input
                                        type="checkbox"
                                        checked={selectedIds.includes(topic.topicId)}
                                        onChange={() => toggleSelectOne(topic.topicId)}
                                        className="w-4 h-4 rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                                    />
                                </td>
                                <td className="px-6 py-4 text-sm text-gray-600 font-medium">#{topic.order}</td>
                                <td className="px-6 py-4">
                                    <div className="text-sm font-bold text-gray-900 group-hover:text-blue-600 transition">{topic.name}</div>
                                    <div className="text-xs text-gray-500 truncate max-w-xs">{topic.description}</div>
                                </td>
                                <td className="px-6 py-4 text-sm">
                                    <span className={`px-3 py-1 rounded-full text-xs font-bold ${
                                        topic.status === "active"
                                        ? "bg-green-100 text-green-600"
                                        : "bg-orange-100 text-orange-600"
                                    }`}>
                                        {topic.status === "active" ? "Hoạt động" : "Tạm ngưng"}
                                    </span>
                                </td>
                                <td className="px-6 py-4 text-right space-x-1">
                                    <button
                                        onClick={() => handleEdit(topic)}
                                        className="p-2 text-blue-600 hover:bg-blue-50 rounded-lg transition"
                                        title="Chỉnh sửa"
                                    >
                                        ✏️
                                    </button>
                                    <button
                                        onClick={() => deleteTopic(topic.topicId)}
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
        <TopicFormModal
          isOpen={isModalOpen}
          onClose={() => setIsModalOpen(false)}
          onSubmit={handleFormSubmit}
          initialData={selectedTopic}
        />
      )}
    </div>
  );
};

export default TopicsPage;
