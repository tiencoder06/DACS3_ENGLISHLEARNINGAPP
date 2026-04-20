import React, { useEffect, useState } from "react";
import { type Topic, type TopicStatus } from "../../models/Topic";

interface TopicFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (data: Partial<Topic>) => Promise<void>;
  initialData?: Topic | null;
}

const TopicFormModal: React.FC<TopicFormModalProps> = ({ isOpen, onClose, onSubmit, initialData }) => {
  const [formData, setFormData] = useState<Partial<Topic>>({
    name: "",
    description: "",
    order: 0,
    status: "active" as TopicStatus
  });
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (isOpen) {
        if (initialData) {
            setFormData({
              name: initialData.name,
              description: initialData.description,
              order: initialData.order,
              status: initialData.status
            });
        } else {
            setFormData({
                name: "",
                description: "",
                order: 0,
                status: "active"
            });
        }
    }
  }, [initialData, isOpen]);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      await onSubmit(formData);
      onClose();
    } catch (err) {
      alert("Lỗi khi lưu chủ đề.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-[9999] flex items-center justify-center overflow-x-hidden overflow-y-auto outline-none focus:outline-none">
      <div className="fixed inset-0 bg-black bg-opacity-50 transition-opacity" onClick={onClose}></div>
      <div className="relative bg-white rounded-2xl shadow-2xl max-w-lg w-full m-4 overflow-hidden transform transition-all">
        <div className="p-6 border-b border-gray-100 flex justify-between items-center bg-gray-50">
          <h3 className="text-xl font-bold text-gray-800">
            {initialData ? "Chỉnh sửa chủ đề" : "Thêm chủ đề mới"}
          </h3>
          <button onClick={onClose} className="text-gray-400 hover:text-gray-600 text-3xl leading-none font-semibold outline-none">&times;</button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-5">
          <div>
            <label className="block text-sm font-bold text-gray-700 mb-2">Tên chủ đề *</label>
            <input
              type="text"
              required
              value={formData.name || ""}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:ring-2 focus:ring-blue-500 outline-none transition-all"
              placeholder="VD: Giao tiếp cơ bản"
            />
          </div>

          <div>
            <label className="block text-sm font-bold text-gray-700 mb-2">Mô tả</label>
            <textarea
              value={formData.description || ""}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:ring-2 focus:ring-blue-500 outline-none h-28 resize-none transition-all"
              placeholder="Nhập mô tả ngắn về chủ đề..."
            />
          </div>

          <div className="grid grid-cols-2 gap-6">
            <div>
              <label className="block text-sm font-bold text-gray-700 mb-2">Thứ tự hiển thị</label>
              <input
                type="number"
                value={formData.order || 0}
                onChange={(e) => setFormData({ ...formData, order: parseInt(e.target.value) || 0 })}
                className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:ring-2 focus:ring-blue-500 outline-none transition-all"
              />
            </div>
            <div>
              <label className="block text-sm font-bold text-gray-700 mb-2">Trạng thái</label>
              <select
                value={formData.status || "active"}
                onChange={(e) => setFormData({ ...formData, status: e.target.value as TopicStatus })}
                className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:ring-2 focus:ring-blue-500 outline-none transition-all bg-white cursor-pointer"
              >
                <option value="active">Hoạt động</option>
                <option value="inactive">Tạm ngưng</option>
              </select>
            </div>
          </div>

          <div className="pt-6 flex gap-4">
            <button type="button" onClick={onClose} className="flex-1 px-6 py-3 border border-gray-200 text-gray-600 rounded-xl hover:bg-gray-50 font-bold">Hủy</button>
            <button type="submit" disabled={loading} className="flex-1 px-6 py-3 bg-blue-600 text-white rounded-xl hover:bg-blue-700 font-bold shadow-lg disabled:opacity-50">
              {loading ? "Đang lưu..." : "Lưu lại"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default TopicFormModal;
