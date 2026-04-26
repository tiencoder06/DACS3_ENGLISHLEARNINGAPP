import React, { useEffect, useState } from "react";
import { type Vocabulary, type VocabularyStatus } from "../../models/Vocabulary";
import { type Lesson } from "../../models/Lesson";
import { getLessons } from "../lessons/lessonService";
import { fetchPronunciation, previewAudio } from "../../utils/pronunciationService";

interface VocabularyFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (data: Partial<Vocabulary>) => Promise<void>;
  initialData?: Vocabulary | null;
}

const VocabularyFormModal: React.FC<VocabularyFormModalProps> = ({ isOpen, onClose, onSubmit, initialData }) => {
  const [formData, setFormData] = useState<Partial<Vocabulary>>({
    lessonId: "",
    word: "",
    meaning: "",
    pronunciation: "",
    partOfSpeech: "",
    exampleSentence: "",
    audioText: "",
    audioUrl: "",
    status: "active" as VocabularyStatus
  });

  const [lessons, setLessons] = useState<Lesson[]>([]);
  const [loading, setLoading] = useState(false);
  const [fetchingLessons, setFetchingLessons] = useState(false);
  const [isAutoFilling, setIsAutoFilling] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const loadLessons = async () => {
      setFetchingLessons(true);
      try {
        const data = await getLessons();
        setLessons(data.filter(l => l.status !== "deleted"));
      } catch (err) {
        console.error("Failed to load lessons", err);
      } finally {
        setFetchingLessons(false);
      }
    };

    if (isOpen) {
      loadLessons();
      if (initialData) {
        setFormData(initialData);
      } else {
        setFormData({
          lessonId: "",
          word: "",
          meaning: "",
          pronunciation: "",
          partOfSpeech: "",
          exampleSentence: "",
          audioText: "",
          audioUrl: "",
          status: "active"
        });
      }
      setError(null);
    }
  }, [initialData, isOpen]);

  const handleAutoFill = async () => {
    if (!formData.word?.trim()) return;

    setIsAutoFilling(true);
    setError(null);
    try {
      const data = await fetchPronunciation(formData.word);
      setFormData(prev => ({
        ...prev,
        pronunciation: data.pronunciation,
        audioUrl: data.audioUrl,
        partOfSpeech: data.partOfSpeech,
        audioText: data.audioText,
        pronunciationSource: data.pronunciationSource
      }));
    } catch (err: any) {
      setError("Không tìm thấy phát âm cho từ này.");
    } finally {
      setIsAutoFilling(false);
    }
  };

  const handlePreview = () => {
    previewAudio(formData.audioUrl, formData.audioText || formData.word);
  };

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.lessonId) {
        alert("Vui lòng chọn bài học");
        return;
    }
    setLoading(true);
    try {
      await onSubmit(formData);
      onClose();
    } catch (err) {
      alert("Lỗi khi lưu từ vựng.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-[9999] flex items-center justify-center overflow-x-hidden overflow-y-auto outline-none focus:outline-none text-left">
      <div className="fixed inset-0 bg-black bg-opacity-50 transition-opacity" onClick={onClose}></div>
      <div className="relative bg-white rounded-2xl shadow-2xl max-w-2xl w-full m-4 overflow-hidden transform transition-all">
        <div className="p-6 border-b border-gray-100 flex justify-between items-center bg-gray-50 text-left">
          <h3 className="text-xl font-bold text-gray-800">
            {initialData ? "Chỉnh sửa từ vựng" : "Thêm từ vựng mới"}
          </h3>
          <button onClick={onClose} className="text-gray-400 hover:text-gray-600 text-3xl leading-none font-semibold outline-none">&times;</button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-4 max-h-[80vh] overflow-y-auto text-left">
          {error && (
            <div className="p-3 bg-red-50 text-red-600 rounded-lg text-sm border border-red-100">
                {error}
            </div>
          )}

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-bold text-gray-700 mb-2">Bài học *</label>
              <select
                required
                value={formData.lessonId}
                onChange={(e) => setFormData({ ...formData, lessonId: e.target.value })}
                className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:ring-2 focus:ring-blue-500 outline-none bg-white"
                disabled={fetchingLessons}
              >
                <option value="">-- Chọn bài học --</option>
                {lessons.map(lesson => (
                    <option key={lesson.lessonId} value={lesson.lessonId}>{lesson.name}</option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-sm font-bold text-gray-700 mb-2">Từ vựng *</label>
              <div className="flex gap-2">
                <input
                    type="text"
                    required
                    value={formData.word || ""}
                    onChange={(e) => setFormData({ ...formData, word: e.target.value })}
                    className="flex-1 px-4 py-3 rounded-xl border border-gray-200 focus:ring-2 focus:ring-blue-500 outline-none"
                    placeholder="VD: Hello"
                />
                <button
                    type="button"
                    onClick={handleAutoFill}
                    disabled={isAutoFilling || !formData.word?.trim()}
                    className="px-4 py-2 bg-blue-50 text-blue-600 rounded-xl hover:bg-blue-100 transition text-xs font-bold disabled:opacity-50"
                >
                    {isAutoFilling ? "Đang lấy..." : "Tự động"}
                </button>
              </div>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-bold text-gray-700 mb-2">Ý nghĩa *</label>
              <input
                type="text"
                required
                value={formData.meaning || ""}
                onChange={(e) => setFormData({ ...formData, meaning: e.target.value })}
                className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:ring-2 focus:ring-blue-500 outline-none"
                placeholder="VD: Xin chào"
              />
            </div>
            <div>
                <label className="block text-sm font-bold text-gray-700 mb-2">Phiên âm</label>
                <input
                    type="text"
                    value={formData.pronunciation || ""}
                    onChange={(e) => setFormData({ ...formData, pronunciation: e.target.value })}
                    className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:ring-2 focus:ring-blue-500 outline-none"
                    placeholder="VD: /həˈloʊ/"
                />
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-bold text-gray-700 mb-2">Loại từ</label>
              <input
                type="text"
                value={formData.partOfSpeech || ""}
                onChange={(e) => setFormData({ ...formData, partOfSpeech: e.target.value })}
                className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:ring-2 focus:ring-blue-500 outline-none"
                placeholder="VD: Noun, Verb, Adj..."
              />
            </div>
            <div>
                <label className="block text-sm font-bold text-gray-700 mb-2">Trạng thái</label>
                <select
                    value={formData.status || "active"}
                    onChange={(e) => setFormData({ ...formData, status: e.target.value as VocabularyStatus })}
                    className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:ring-2 focus:ring-blue-500 outline-none bg-white"
                >
                    <option value="active">Hoạt động</option>
                    <option value="inactive">Tạm ngưng</option>
                </select>
            </div>
          </div>

          <div>
            <label className="block text-sm font-bold text-gray-700 mb-2">Câu ví dụ</label>
            <textarea
              value={formData.exampleSentence || ""}
              onChange={(e) => setFormData({ ...formData, exampleSentence: e.target.value })}
              className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:ring-2 focus:ring-blue-500 outline-none h-20 resize-none"
              placeholder="VD: Hello, how are you?"
            />
          </div>

          <div className="bg-blue-50 p-4 rounded-xl space-y-4">
            <div className="flex justify-between items-center">
                <h4 className="text-sm font-bold text-blue-800">Cấu hình phát âm (Audio)</h4>
                <button
                    type="button"
                    onClick={handlePreview}
                    className="text-xs font-bold text-blue-600 hover:underline"
                >
                    🔊 Nghe thử
                </button>
            </div>
            <div>
                <label className="block text-xs font-bold text-blue-600 mb-1">Văn bản phát âm (Để trống sẽ mặc định lấy từ vựng)</label>
                <input
                    type="text"
                    value={formData.audioText || ""}
                    onChange={(e) => setFormData({ ...formData, audioText: e.target.value })}
                    className="w-full px-4 py-2 rounded-lg border border-blue-100 focus:ring-2 focus:ring-blue-500 outline-none"
                    placeholder="VD: Hello"
                />
            </div>
            <div>
                <label className="block text-xs font-bold text-blue-600 mb-1">Link file âm thanh (Để trống sẽ dùng TTS)</label>
                <input
                    type="text"
                    value={formData.audioUrl || ""}
                    onChange={(e) => setFormData({ ...formData, audioUrl: e.target.value })}
                    className="w-full px-4 py-2 rounded-lg border border-blue-100 focus:ring-2 focus:ring-blue-500 outline-none"
                    placeholder="https://example.com/audio.mp3"
                />
            </div>
          </div>

          <div className="pt-6 flex gap-4 text-left">
            <button type="button" onClick={onClose} className="flex-1 px-6 py-3 border border-gray-200 text-gray-600 rounded-xl hover:bg-gray-50 font-bold">Hủy</button>
            <button type="submit" disabled={loading} className="flex-1 px-6 py-3 bg-blue-600 text-white rounded-xl hover:bg-blue-700 font-bold shadow-lg disabled:opacity-50">
              {loading ? "Đang lưu..." : "Lưu từ vựng"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default VocabularyFormModal;
