import { useState, useEffect, useCallback } from "react";
import { type Vocabulary } from "../../models/Vocabulary";
import * as vocabularyService from "./vocabularyService";
import { useAuth } from "../../context/AuthContext";

export const useVocabulary = () => {
  const [vocabularyItems, setVocabularyItems] = useState<Vocabulary[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const { currentUser } = useAuth();

  const loadVocabulary = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await vocabularyService.getVocabulary();
      setVocabularyItems(data);
    } catch (err: any) {
      setError("Không thể tải danh sách từ vựng.");
      console.error(err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadVocabulary();
  }, [loadVocabulary]);

  const handleCreateVocabulary = async (data: Partial<Vocabulary>) => {
    if (!currentUser) return;
    try {
      await vocabularyService.createVocabulary(data, currentUser.uid);
      await loadVocabulary();
    } catch (err) {
      console.error(err);
      throw new Error("Không thể tạo từ vựng mới.");
    }
  };

  const handleUpdateVocabulary = async (vocabId: string, data: Partial<Vocabulary>) => {
    if (!currentUser) return;
    try {
      await vocabularyService.updateVocabulary(vocabId, data, currentUser.uid);
      await loadVocabulary();
    } catch (err) {
      console.error(err);
      throw new Error("Không thể cập nhật từ vựng.");
    }
  };

  const handleDeleteVocabulary = async (vocabId: string) => {
    if (!currentUser) return;
    if (!window.confirm("Bạn có chắc chắn muốn xóa từ vựng này?")) return;
    try {
      await vocabularyService.softDeleteVocabulary(vocabId, currentUser.uid);
      await loadVocabulary();
    } catch (err) {
      console.error(err);
      throw new Error("Không thể xóa từ vựng.");
    }
  };

  return {
    vocabularyItems,
    loading,
    error,
    reloadVocabulary: loadVocabulary,
    createVocabulary: handleCreateVocabulary,
    updateVocabulary: handleUpdateVocabulary,
    deleteVocabulary: handleDeleteVocabulary
  };
};
