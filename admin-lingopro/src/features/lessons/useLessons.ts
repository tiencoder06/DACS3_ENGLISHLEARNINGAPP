import { useState, useEffect, useCallback } from "react";
import { type Lesson } from "../../models/Lesson";
import * as lessonService from "./lessonService";
import { useAuth } from "../../context/AuthContext";

export const useLessons = () => {
  const [lessons, setLessons] = useState<Lesson[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const { currentUser } = useAuth();

  const loadLessons = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await lessonService.getLessons();
      setLessons(data);
    } catch (err: any) {
      setError("Không thể tải danh sách bài học.");
      console.error(err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadLessons();
  }, [loadLessons]);

  const handleCreateLesson = async (data: Partial<Lesson>) => {
    if (!currentUser) return;
    try {
      await lessonService.createLesson(data, currentUser.uid);
      await loadLessons();
    } catch (err) {
      console.error(err);
      throw new Error("Không thể tạo bài học mới.");
    }
  };

  const handleUpdateLesson = async (lessonId: string, data: Partial<Lesson>) => {
    if (!currentUser) return;
    try {
      await lessonService.updateLesson(lessonId, data, currentUser.uid);
      await loadLessons();
    } catch (err) {
      console.error(err);
      throw new Error("Không thể cập nhật bài học.");
    }
  };

  const handleDeleteLesson = async (lessonId: string) => {
    if (!currentUser) return;
    if (!window.confirm("Bạn có chắc chắn muốn xóa bài học này?")) return;
    try {
      await lessonService.softDeleteLesson(lessonId, currentUser.uid);
      await loadLessons();
    } catch (err) {
      console.error(err);
      throw new Error("Không thể xóa bài học.");
    }
  };

  return {
    lessons,
    loading,
    error,
    reloadLessons: loadLessons,
    createLesson: handleCreateLesson,
    updateLesson: handleUpdateLesson,
    deleteLesson: handleDeleteLesson
  };
};
