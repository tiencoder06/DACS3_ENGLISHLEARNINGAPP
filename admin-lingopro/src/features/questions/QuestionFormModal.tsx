import React, { useEffect, useState, useMemo } from "react";
import { type Question, type QuestionType, type QuestionUsage, type QuestionStatus } from "../../models/Question";
import { type Lesson } from "../../models/Lesson";
import { type Vocabulary } from "../../models/Vocabulary";
import { getLessons } from "../lessons/lessonService";
import { getVocabulary } from "../vocabulary/vocabularyService";

interface QuestionFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (data: Partial<Question>) => Promise<void>;
  initialData?: Question | null;
}

const QuestionFormModal: React.FC<QuestionFormModalProps> = ({ isOpen, onClose, onSubmit, initialData }) => {
  const [formData, setFormData] = useState<Partial<Question>>({
    lessonId: "",
    vocabId: "",
    questionType: "multiple_choice" as QuestionType,
    usage: "both" as QuestionUsage,
    questionText: "",
    options: ["", "", "", ""],
    correctAnswer: "",
    explanation: "",
    status: "active" as QuestionStatus
  });

  const [lessons, setLessons] = useState<Lesson[]>([]);
  const [vocabulary, setVocabulary] = useState<Vocabulary[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const loadData = async () => {
      try {
        const [lessonsData, vocabData] = await Promise.all([getLessons(), getVocabulary()]);
        setLessons(lessonsData);
        setVocabulary(vocabData);
      } catch (err) {
        console.error("Failed to load dependency data", err);
      }
    };
    if (isOpen) loadData();
  }, [isOpen]);

  useEffect(() => {
    if (isOpen) {
      if (initialData) {
        // Ensure exactly 4 options for UI inputs
        const normalizedOptions = [...(initialData.options || [])];
        while (normalizedOptions.length < 4) normalizedOptions.push("");
        setFormData({ ...initialData, options: normalizedOptions });
      } else {
        setFormData({
          lessonId: "",
          vocabId: "",
          questionType: "multiple_choice",
          usage: "both",
          questionText: "",
          options: ["", "", "", ""],
          correctAnswer: "",
          explanation: "",
          status: "active"
        });
      }
    }
  }, [initialData, isOpen]);

  const filteredVocabulary = useMemo(() => {
    return vocabulary.filter(v => v.lessonId === formData.lessonId);
  }, [vocabulary, formData.lessonId]);

  // Handle option changes
  const handleOptionChange = (index: number, value: string) => {
    const newOptions = [...(formData.options || ["", "", "", ""])];
    newOptions[index] = value;

    // If correct answer was this option, keep it synced or reset if it no longer matches
    setFormData(prev => {
        const next = { ...prev, options: newOptions };
        if (prev.questionType !== "fill_blank") {
            // Check if current correctAnswer is still in the new options list
            if (prev.correctAnswer && !newOptions.includes(prev.correctAnswer)) {
                next.correctAnswer = "";
            }
        }
        return next;
    });
  };

  const handleTypeChange = (type: QuestionType) => {
    setFormData(prev => ({
        ...prev,
        questionType: type,
        options: type === "fill_blank" ? [] : (prev.options?.length === 4 ? prev.options : ["", "", "", ""]),
        correctAnswer: "" // Reset correct answer on type change to be safe
    }));
  };

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      const finalData = { ...formData };
      if (formData.questionType !== "fill_blank") {
          finalData.options = (formData.options || []).map(o => o.trim());
      }
      await onSubmit(finalData);
      onClose();
    } catch (err) {
      alert("Lỗi khi lưu câu hỏi.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-[9999] flex items-center justify-center overflow-x-hidden overflow-y-auto outline-none focus:outline-none text-left">
      <div className="fixed inset-0 bg-black bg-opacity-50 transition-opacity" onClick={onClose}></div>
      <div className="relative bg-white rounded-2xl shadow-2xl max-w-3xl w-full m-4 overflow-hidden transform transition-all">
        <div className="p-6 border-b border-gray-100 flex justify-between items-center bg-gray-50">
          <h3 className="text-xl font-bold text-gray-800">
            {initialData ? "Chỉnh sửa câu hỏi" : "Thêm câu hỏi mới"}
          </h3>
          <button onClick={onClose} className="text-gray-400 hover:text-gray-600 text-3xl leading-none font-semibold outline-none">&times;</button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-4 max-h-[85vh] overflow-y-auto">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-bold text-gray-700 mb-2">Bài học *</label>
              <select
                required
                value={formData.lessonId}
                onChange={(e) => setFormData({ ...formData, lessonId: e.target.value, vocabId: "" })}
                className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:ring-2 focus:ring-blue-500 outline-none bg-white"
              >
                <option value="">-- Chọn bài học --</option>
                {lessons.map(l => (
                    <option key={l.lessonId} value={l.lessonId}>{l.name}</option>
                ))}
              </select>
            </div>
            <div>
              <label className="block text-sm font-bold text-gray-700 mb-2">Từ vựng (Liên kết)</label>
              <select
                value={formData.vocabId}
                onChange={(e) => setFormData({ ...formData, vocabId: e.target.value })}
                className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:ring-2 focus:ring-blue-500 outline-none bg-white"
                disabled={!formData.lessonId}
              >
                <option value="">-- Chọn từ vựng (không bắt buộc) --</option>
                {filteredVocabulary.map(v => (
                    <option key={v.vocabId} value={v.vocabId}>{v.word}</option>
                ))}
              </select>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-bold text-gray-700 mb-2">Loại câu hỏi *</label>
              <select
                required
                value={formData.questionType}
                onChange={(e) => handleTypeChange(e.target.value as QuestionType)}
                className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:ring-2 focus:ring-blue-500 outline-none bg-white"
              >
                <option value="multiple_choice">Trắc nghiệm</option>
                <option value="fill_blank">Điền vào chỗ trống</option>
                <option value="listen_choose">Nghe và chọn</option>
              </select>
            </div>
            <div>
              <label className="block text-sm font-bold text-gray-700 mb-2">Mục đích sử dụng *</label>
              <select
                required
                value={formData.usage}
                onChange={(e) => setFormData({ ...formData, usage: e.target.value as QuestionUsage })}
                className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:ring-2 focus:ring-blue-500 outline-none bg-white"
              >
                <option value="both">Cả hai (Luyện tập & Quiz)</option>
                <option value="practice">Chỉ Luyện tập</option>
                <option value="quiz">Chỉ Quiz</option>
              </select>
            </div>
          </div>

          <div>
            <label className="block text-sm font-bold text-gray-700 mb-2">Nội dung câu hỏi *</label>
            <textarea
              required
              value={formData.questionText || ""}
              onChange={(e) => setFormData({ ...formData, questionText: e.target.value })}
              className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:ring-2 focus:ring-blue-500 outline-none h-24 resize-none"
              placeholder="VD: Nghĩa của từ 'Hello' là gì?"
            />
          </div>

          {formData.questionType !== "fill_blank" && (
            <div className="bg-gray-50 p-4 rounded-xl space-y-4">
                <label className="block text-sm font-bold text-gray-700">Các lựa chọn trả lời *</label>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                    {["A", "B", "C", "D"].map((label, idx) => (
                        <div key={label} className="flex items-center gap-2">
                            <span className="font-bold text-blue-600">{label}:</span>
                            <input
                                type="text"
                                required={formData.questionType !== "fill_blank"}
                                value={formData.options?.[idx] || ""}
                                onChange={(e) => handleOptionChange(idx, e.target.value)}
                                className="flex-1 px-3 py-2 rounded-lg border border-gray-200 focus:ring-2 focus:ring-blue-500 outline-none"
                                placeholder={`Lựa chọn ${label}`}
                            />
                        </div>
                    ))}
                </div>
            </div>
          )}

          <div>
            <label className="block text-sm font-bold text-gray-700 mb-2">Đáp án đúng *</label>
            {formData.questionType === "fill_blank" ? (
                <input
                    type="text"
                    required
                    value={formData.correctAnswer || ""}
                    onChange={(e) => setFormData({ ...formData, correctAnswer: e.target.value })}
                    className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:ring-2 focus:ring-blue-500 outline-none"
                    placeholder="Nhập đáp án đúng..."
                />
            ) : (
                <select
                    required
                    value={formData.correctAnswer}
                    onChange={(e) => setFormData({ ...formData, correctAnswer: e.target.value })}
                    className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:ring-2 focus:ring-blue-500 outline-none bg-white"
                >
                    <option value="">-- Chọn đáp án đúng --</option>
                    {formData.options?.filter(o => o.trim() !== "").map((opt, idx) => (
                        <option key={idx} value={opt}>{opt}</option>
                    ))}
                </select>
            )}
          </div>

          <div>
            <label className="block text-sm font-bold text-gray-700 mb-2">Giải thích đáp án</label>
            <textarea
              value={formData.explanation || ""}
              onChange={(e) => setFormData({ ...formData, explanation: e.target.value })}
              className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:ring-2 focus:ring-blue-500 outline-none h-20 resize-none"
              placeholder="Giải thích tại sao đáp án này đúng..."
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
             <div>
                <label className="block text-sm font-bold text-gray-700 mb-2">Trạng thái</label>
                <select
                    value={formData.status}
                    onChange={(e) => setFormData({ ...formData, status: e.target.value as QuestionStatus })}
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
              {loading ? "Đang lưu..." : "Lưu câu hỏi"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default QuestionFormModal;
