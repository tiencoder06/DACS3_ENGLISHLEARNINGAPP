import React, { useEffect, useState } from "react";
import { type Lesson, type LessonStatus } from "../../models/Lesson";
import { type Topic } from "../../models/Topic";
import { getTopics } from "../topics/topicService";

interface LessonFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (data: Partial<Lesson>) => Promise<void>;
  initialData?: Lesson | null;
}

const LessonFormModal: React.FC<LessonFormModalProps> = ({ isOpen, onClose, onSubmit, initialData }) => {
  const [formData, setFormData] = useState<Partial<Lesson>>({
    topicId: "",
    name: "",
    description: "",
    content: "",
    order: 0,
    totalWords: 0,
    status: "active" as LessonStatus
  });

  const [topics, setTopics] = useState<Topic[]>([]);
  const [loading, setLoading] = useState(false);
  const [fetchingTopics, setFetchingTopics] = useState(false);

  useEffect(() => {
    const loadTopics = async () => {
      setFetchingTopics(true);
      try {
        const data = await getTopics();
        setTopics(data.filter(t => t.status !== "deleted"));
      } catch (err) {
        console.error("Failed to load topics", err);
      } finally {
        setFetchingTopics(false);
      }
    };

    if (isOpen) {
      loadTopics();
      if (initialData) {
        setFormData(initialData);
      } else {
        setFormData({
          topicId: "",
          name: "",
          description: "",
          content: "",
          order: 0,
          totalWords: 0,
          status: "active"
        });
      }
    }
  }, [initialData, isOpen]);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.topicId) {
        alert("Vui lòng chọn chủ đề");
        return;
    }
    setLoading(true);
    try {
      await onSubmit(formData);
      onClose();
    } catch (err) {
      alert("Lỗi khi lưu bài học.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-[9999] flex items-center justify-center overflow-x-hidden overflow-y-auto outline-none focus:outline-none">
      <div className="fixed inset-0 bg-black bg-opacity-50 transition-opacity" onClick={onClose}></div>
      <div className="relative bg-white rounded-2xl shadow-2xl max-w-2xl w-full m-4 overflow-hidden transform transition-all">
        <div className="p-6 border-b border-gray-100 flex justify-between items-center bg-gray-50">
          <h3 className="text-xl font-bold text-gray-800">
            {initialData ? "Chỉnh sửa bài học" : "Thêm bài học mới"}
          </h3>
          <button onClick={onClose} className="text-gray-400 hover:text-gray-600 text-3xl leading-none font-semibold outline-none">&times;</button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-4 max-h-[70vh] overflow-y-auto">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-bold text-gray-700 mb-2">Chủ đề *</label>
              <select
                required
                value={formData.topicId}
                onChange={(e) => setFormData({ ...formData, topicId: e.target.value })}
                className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:ring-2 focus:ring-blue-500 outline-none bg-white"
                disabled={fetchingTopics}
              >
                <option value="">-- Chọn chủ đề --</option>
                {topics.map(topic => (
                    <option key={topic.topicId} value={topic.topicId}>{topic.name}</option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-sm font-bold text-gray-700 mb-2">Tên bài học *</label>
              <input
                type="text"
                required
                value={formData.name || ""}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:ring-2 focus:ring-blue-500 outline-none"
                placeholder="VD: Chào hỏi cơ bản"
              />
            </div>
          </div>

          <div>
            <label className="block text-sm font-bold text-gray-700 mb-2">Mô tả ngắn</label>
            <input
              type="text"
              value={formData.description || ""}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:ring-2 focus:ring-blue-500 outline-none"
              placeholder="VD: Học cách nói xin chào và giới thiệu bản thân"
            />
          </div>

          <div>
            <label className="block text-sm font-bold text-gray-700 mb-2">Nội dung chi tiết</label>
            <textarea
              value={formData.content || ""}
              onChange={(e) => setFormData({ ...formData, content: e.target.value })}
              className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:ring-2 focus:ring-blue-500 outline-none h-32 resize-none"
              placeholder="Nhập nội dung bài học..."
            />
          </div>

          <div className="grid grid-cols-3 gap-4">
            <div>
              <label className="block text-sm font-bold text-gray-700 mb-2">Thứ tự</label>
              <input
                type="number"
                value={formData.order || 0}
                onChange={(e) => setFormData({ ...formData, order: parseInt(e.target.value) || 0 })}
                className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:ring-2 focus:ring-blue-500 outline-none"
              />
            </div>
            <div>
              <label className="block text-sm font-bold text-gray-700 mb-2">Số từ vựng</label>
              <input
                type="number"
                value={formData.totalWords || 0}
                onChange={(e) => setFormData({ ...formData, totalWords: parseInt(e.target.value) || 0 })}
                className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:ring-2 focus:ring-blue-500 outline-none"
              />
            </div>
            <div>
              <label className="block text-sm font-bold text-gray-700 mb-2">Trạng thái</label>
              <select
                value={formData.status || "active"}
                onChange={(e) => setFormData({ ...formData, status: e.target.value as LessonStatus })}
                className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:ring-2 focus:ring-blue-500 outline-none bg-white"
              >
                <option value="active">Hoạt động</option>
                <option value="inactive">Tạm ngưng</option>
              </select>
            </div>
          </div>

          <div className="pt-6 flex gap-4">
            <button type="button" onClick={onClose} className="flex-1 px-6 py-3 border border-gray-200 text-gray-600 rounded-xl hover:bg-gray-50 font-bold">Hủy</button>
            <button type="submit" disabled={loading} className="flex-1 px-6 py-3 bg-blue-600 text-white rounded-xl hover:bg-blue-700 font-bold shadow-lg disabled:opacity-50">
              {loading ? "Đang lưu..." : "Lưu bài học"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default LessonFormModal;
