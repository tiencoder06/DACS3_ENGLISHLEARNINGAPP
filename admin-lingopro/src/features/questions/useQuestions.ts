import { useState, useEffect, useCallback } from "react";
import { type Question } from "../../models/Question";
import * as questionService from "./questionService";
import { useAuth } from "../../context/AuthContext";

export const useQuestions = () => {
  const [questions, setQuestions] = useState<Question[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const { currentUser } = useAuth();

  const loadQuestions = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await questionService.getQuestions();
      setQuestions(data);
    } catch (err: any) {
      setError("Không thể tải danh sách câu hỏi.");
      console.error(err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadQuestions();
  }, [loadQuestions]);

  const handleCreateQuestion = async (data: Partial<Question>) => {
    if (!currentUser) return;
    try {
      await questionService.createQuestion(data, currentUser.uid);
      await loadQuestions();
    } catch (err) {
      console.error(err);
      throw new Error("Không thể tạo câu hỏi mới.");
    }
  };

  const handleUpdateQuestion = async (questionId: string, data: Partial<Question>) => {
    if (!currentUser) return;
    try {
      await questionService.updateQuestion(questionId, data, currentUser.uid);
      await loadQuestions();
    } catch (err) {
      console.error(err);
      throw new Error("Không thể cập nhật câu hỏi.");
    }
  };

  const handleDeleteQuestion = async (questionId: string) => {
    if (!currentUser) return;
    if (!window.confirm("Bạn có chắc chắn muốn xóa câu hỏi này?")) return;
    try {
      await questionService.softDeleteQuestion(questionId, currentUser.uid);
      await loadQuestions();
    } catch (err) {
      console.error(err);
      throw new Error("Không thể xóa câu hỏi.");
    }
  };

  return {
    questions,
    loading,
    error,
    reloadQuestions: loadQuestions,
    createQuestion: handleCreateQuestion,
    updateQuestion: handleUpdateQuestion,
    deleteQuestion: handleDeleteQuestion
  };
};
