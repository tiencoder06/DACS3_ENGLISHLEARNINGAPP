import React, { useState, useMemo, useEffect } from "react";
import { useQuestions } from "./useQuestions";
import { getLessons } from "../lessons/lessonService";
import { getVocabulary } from "../vocabulary/vocabularyService";
import { getTopics } from "../topics/topicService";
import { type Lesson } from "../../models/Lesson";
import { type Vocabulary } from "../../models/Vocabulary";
import { type Topic } from "../../models/Topic";
import { type Question, type QuestionType, type QuestionUsage, type QuestionStatus } from "../../models/Question";
import QuestionFormModal from "./QuestionFormModal";

const QuestionsPage: React.FC = () => {
  const {
    questions,
    loading,
    error,
    reloadQuestions,
    createQuestion,
    updateQuestion,
    deleteQuestion
  } = useQuestions();

  const [topics, setTopics] = useState<Topic[]>([]);
  const [lessons, setLessons] = useState<Lesson[]>([]);
  const [vocabulary, setVocabulary] = useState<Vocabulary[]>([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [topicFilter, setTopicFilter] = useState<string>("all");
  const [lessonFilter, setLessonFilter] = useState<string>("all");
  const [vocabFilter, setVocabFilter] = useState<string>("all");
  const [typeFilter, setTypeFilter] = useState<QuestionType | "all">("all");
  const [usageFilter, setUsageFilter] = useState<QuestionUsage | "all">("all");
  const [statusFilter, setStatusFilter] = useState<QuestionStatus | "all">("all");

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedQuestion, setSelectedQuestion] = useState<Question | null>(null);

  useEffect(() => {
    const loadDependencies = async () => {
      try {
        const [tData, lData, vData] = await Promise.all([getTopics(), getLessons(), getVocabulary()]);
        setTopics(tData);
        setLessons(lData);
        setVocabulary(vData);
      } catch (err) {
        console.error("Failed to load dependencies", err);
      }
    };
    loadDependencies();
  }, []);

  const topicMap = useMemo(() => {
    const map: Record<string, string> = {};
    topics.forEach(t => map[t.topicId] = t.name);
    return map;
  }, [topics]);

  const lessonMap = useMemo(() => {
    const map: Record<string, string> = {};
    lessons.forEach(l => map[l.lessonId] = l.name);
    return map;
  }, [lessons]);

  const vocabMap = useMemo(() => {
    const map: Record<string, string> = {};
    vocabulary.forEach(v => map[v.vocabId] = v.word);
    return map;
  }, [vocabulary]);

  const filteredQuestions = questions.filter(q => {
    const matchesSearch = q.questionText.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         q.correctAnswer.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesTopic = topicFilter === "all" || q.topicId === topicFilter;
    const matchesLesson = lessonFilter === "all" || q.lessonId === lessonFilter;
    const matchesVocab = vocabFilter === "all" || q.vocabId === vocabFilter;
    const matchesType = typeFilter === "all" || q.questionType === typeFilter;
    const matchesUsage = usageFilter === "all" || q.usage === usageFilter;
    const matchesStatus = statusFilter === "all" || q.status === statusFilter;

    return matchesSearch && matchesTopic && matchesLesson && matchesVocab && matchesType && matchesUsage && matchesStatus;
  });

  const handleAddNew = () => {
    setSelectedQuestion(null);
    setIsModalOpen(true);
  };

  const handleEdit = (question: Question) => {
    setSelectedQuestion(question);
    setIsModalOpen(true);
  };

  const handleFormSubmit = async (data: Partial<Question>) => {
    if (selectedQuestion) {
      await updateQuestion(selectedQuestion.questionId, data);
    } else {
      await createQuestion(data);
    }
  };

  const formatType = (type: string) => {
    switch (type) {
        case "multiple_choice": return "Trắc nghiệm";
        case "fill_blank": return "Điền khuyết";
        case "listen_choose": return "Nghe chọn";
        default: return type;
    }
  };

  return (
    <div className="space-y-6 text-left">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <h2 className="text-2xl font-bold text-gray-800">Quản lý câu hỏi</h2>
        <div className="flex items-center gap-3">
            <button onClick={reloadQuestions} className="p-2 bg-white border border-gray-200 rounded-xl hover:bg-gray-50 transition shadow-sm">🔄</button>
            <button onClick={handleAddNew} className="px-6 py-2.5 bg-blue-600 text-white rounded-xl font-bold hover:bg-blue-700 transition shadow-lg shadow-blue-200">
                + Thêm câu hỏi mới
            </button>
        </div>
      </div>

      <div className="bg-white p-6 rounded-2xl shadow-sm border border-gray-100 space-y-4">
        <div className="flex-1 relative">
            <span className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400">🔍</span>
            <input
                type="text"
                placeholder="Tìm kiếm nội dung câu hỏi hoặc đáp án..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full pl-10 pr-4 py-2.5 rounded-xl border border-gray-200 focus:ring-2 focus:ring-blue-500 outline-none transition"
            />
        </div>
        <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-6 gap-4">
            <select value={topicFilter} onChange={(e) => setTopicFilter(e.target.value)} className="px-3 py-2 rounded-lg border border-gray-200 text-sm outline-none">
                <option value="all">Tất cả chủ đề</option>
                {topics.map(t => <option key={t.topicId} value={t.topicId}>{t.name}</option>)}
            </select>
            <select value={lessonFilter} onChange={(e) => setLessonFilter(e.target.value)} className="px-3 py-2 rounded-lg border border-gray-200 text-sm outline-none">
                <option value="all">Tất cả bài học</option>
                {lessons.map(l => <option key={l.lessonId} value={l.lessonId}>{l.name}</option>)}
            </select>
            <select value={vocabFilter} onChange={(e) => setVocabFilter(e.target.value)} className="px-3 py-2 rounded-lg border border-gray-200 text-sm outline-none">
                <option value="all">Tất cả từ vựng</option>
                {vocabulary.map(v => <option key={v.vocabId} value={v.vocabId}>{v.word}</option>)}
            </select>
            <select value={typeFilter} onChange={(e) => setTypeFilter(e.target.value as any)} className="px-3 py-2 rounded-lg border border-gray-200 text-sm outline-none">
                <option value="all">Tất cả loại</option>
                <option value="multiple_choice">Trắc nghiệm</option>
                <option value="fill_blank">Điền khuyết</option>
                <option value="listen_choose">Nghe chọn</option>
            </select>
            <select value={usageFilter} onChange={(e) => setUsageFilter(e.target.value as any)} className="px-3 py-2 rounded-lg border border-gray-200 text-sm outline-none">
                <option value="all">Tất cả mục đích</option>
                <option value="both">Cả hai</option>
                <option value="practice">Luyện tập</option>
                <option value="quiz">Quiz</option>
            </select>
            <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value as any)} className="px-3 py-2 rounded-lg border border-gray-200 text-sm outline-none">
                <option value="all">Tất cả trạng thái</option>
                <option value="active">Hoạt động</option>
                <option value="inactive">Tạm ngưng</option>
            </select>
        </div>
      </div>

      <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
        {loading ? (
            <div className="p-12 flex justify-center"><div className="animate-spin rounded-full h-10 w-10 border-t-2 border-b-2 border-blue-600"></div></div>
        ) : error ? (
            <div className="p-12 text-center text-red-500 italic">{error}</div>
        ) : filteredQuestions.length === 0 ? (
            <div className="p-12 text-center text-gray-400">Không tìm thấy câu hỏi nào.</div>
        ) : (
            <div className="overflow-x-auto">
                <table className="w-full text-left border-collapse">
                    <thead className="bg-gray-50 border-b border-gray-100">
                        <tr>
                            <th className="px-6 py-4 text-xs font-bold text-gray-500 uppercase">Câu hỏi</th>
                            <th className="px-6 py-4 text-xs font-bold text-gray-500 uppercase">Thông tin liên kết</th>
                            <th className="px-6 py-4 text-xs font-bold text-gray-500 uppercase">Loại / Mục đích</th>
                            <th className="px-6 py-4 text-xs font-bold text-gray-500 uppercase">Đáp án</th>
                            <th className="px-6 py-4 text-xs font-bold text-gray-500 uppercase text-right">Thao tác</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-100">
                        {filteredQuestions.map((q) => (
                            <tr key={q.questionId} className="hover:bg-gray-50 transition group">
                                <td className="px-6 py-4">
                                    <div className="text-sm font-bold text-gray-900 line-clamp-2">{q.questionText}</div>
                                    <div className={`mt-1 text-[10px] font-bold px-2 py-0.5 rounded-full inline-block ${q.status === 'active' ? 'bg-green-100 text-green-600' : 'bg-gray-100 text-gray-500'}`}>
                                        {q.status.toUpperCase()}
                                    </div>
                                </td>
                                <td className="px-6 py-4 space-y-1">
                                    <div className="text-[10px] text-purple-600 font-bold uppercase">{topicMap[q.topicId] || "No Topic"}</div>
                                    <div className="text-xs text-gray-600 font-medium">{lessonMap[q.lessonId] || "No Lesson"}</div>
                                    <div className="text-xs text-blue-600 italic">{vocabMap[q.vocabId] || "No vocab"}</div>
                                </td>
                                <td className="px-6 py-4">
                                    <div className="text-xs font-bold text-gray-700">{formatType(q.questionType)}</div>
                                    <div className="text-[10px] text-gray-500 uppercase">{q.usage}</div>
                                </td>
                                <td className="px-6 py-4">
                                    <div className="text-sm text-green-600 font-bold">{q.correctAnswer}</div>
                                </td>
                                <td className="px-6 py-4 text-right space-x-2">
                                    <button onClick={() => handleEdit(q)} className="p-2 text-blue-600 hover:bg-blue-50 rounded-lg transition">✏️</button>
                                    <button onClick={() => deleteQuestion(q.questionId)} className="p-2 text-red-600 hover:bg-red-50 rounded-lg transition">🗑️</button>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        )}
      </div>

      {isModalOpen && (
        <QuestionFormModal
          isOpen={isModalOpen}
          onClose={() => setIsModalOpen(false)}
          onSubmit={handleFormSubmit}
          initialData={selectedQuestion}
        />
      )}
    </div>
  );
};

export default QuestionsPage;
