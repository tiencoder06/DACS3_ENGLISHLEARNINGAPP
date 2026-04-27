import { useState, useEffect, useCallback } from "react";
import { type Question } from "../../models/Question";
import * as questionService from "./questionService";
import { auth } from "../../firebase/firebase";

export const useQuestions = () => {
  const [questions, setQuestions] = useState<Question[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchQuestions = useCallback(async () => {
    setLoading(true);
    try {
      const data = await questionService.getQuestions();
      setQuestions(data);
      setError(null);
    } catch (err) {
      setError("Không thể tải danh sách câu hỏi.");
      console.error(err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchQuestions();
  }, [fetchQuestions]);

  const createQuestion = async (data: Partial<Question>) => {
    const adminUid = auth.currentUser?.uid;
    if (!adminUid) return;
    try {
      await questionService.createQuestion(data, adminUid);
      await fetchQuestions();
    } catch (err) {
      console.error("Create failed", err);
      throw err;
    }
  };

  const updateQuestion = async (id: string, data: Partial<Question>) => {
    const adminUid = auth.currentUser?.uid;
    if (!adminUid) return;
    try {
      await questionService.updateQuestion(id, data, adminUid);
      await fetchQuestions();
    } catch (err) {
      console.error("Update failed", err);
      throw err;
    }
  };

  const deleteQuestion = async (id: string) => {
    const adminUid = auth.currentUser?.uid;
    if (!adminUid) return;
    if (!window.confirm("Bạn có chắc chắn muốn xóa câu hỏi này?")) return;
    try {
      await questionService.softDeleteQuestion(id, adminUid);
      await fetchQuestions();
    } catch (err) {
      console.error("Delete failed", err);
      throw err;
    }
  };

  return {
    questions,
    loading,
    error,
    reloadQuestions: fetchQuestions,
    createQuestion,
    updateQuestion,
    deleteQuestion
  };
};
