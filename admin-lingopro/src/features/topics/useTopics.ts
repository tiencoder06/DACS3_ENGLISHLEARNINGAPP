import { useState, useEffect, useCallback } from "react";
import { type Topic } from "../../models/Topic";
import * as topicService from "./topicService";
import { useAuth } from "../../context/AuthContext";

export const useTopics = () => {
  const [topics, setTopics] = useState<Topic[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const { currentUser } = useAuth();

  const loadTopics = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await topicService.getTopics();
      setTopics(data);
    } catch (err: any) {
      setError("Không thể tải danh sách chủ đề.");
      console.error(err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadTopics();
  }, [loadTopics]);

  const handleCreateTopic = async (data: Partial<Topic>) => {
    if (!currentUser) return;
    try {
      await topicService.createTopic(data, currentUser.uid);
      await loadTopics();
    } catch (err) {
      console.error(err);
      throw new Error("Không thể tạo chủ đề mới.");
    }
  };

  const handleUpdateTopic = async (topicId: string, data: Partial<Topic>) => {
    if (!currentUser) return;
    try {
      await topicService.updateTopic(topicId, data, currentUser.uid);
      await loadTopics();
    } catch (err) {
      console.error(err);
      throw new Error("Không thể cập nhật chủ đề.");
    }
  };

  const handleDeleteTopic = async (topicId: string) => {
    if (!currentUser) return;
    if (!window.confirm("Bạn có chắc chắn muốn xóa chủ đề này?")) return;
    try {
      await topicService.softDeleteTopic(topicId, currentUser.uid);
      await loadTopics();
    } catch (err) {
      console.error(err);
      throw new Error("Không thể xóa chủ đề.");
    }
  };

  return {
    topics,
    loading,
    error,
    reload: loadTopics,
    createTopic: handleCreateTopic,
    updateTopic: handleUpdateTopic,
    deleteTopic: handleDeleteTopic
  };
};
