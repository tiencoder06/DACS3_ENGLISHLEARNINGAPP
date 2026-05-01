import {
  collection,
  getDocs,
  addDoc,
  updateDoc,
  doc,
  serverTimestamp,
  query,
  orderBy,
  where,
  limit
} from "firebase/firestore";
import { db } from "../../firebase/firebase";
import type { Vocabulary } from "../../models/Vocabulary";

const VOCABULARIES_COLLECTION = "vocabularies";
const TOPICS_COLLECTION = "topics";
const LESSONS_COLLECTION = "lessons";

export const getVocabulary = async (): Promise<Vocabulary[]> => {
  try {
    const q = query(
      collection(db, VOCABULARIES_COLLECTION),
      orderBy("word", "asc")
    );

    const snapshot = await getDocs(q);
    return snapshot.docs
      .map(d => ({ ...d.data(), vocabId: d.id } as Vocabulary))
      .filter(v => v.status !== "deleted");
  } catch (error) {
    console.error("Error fetching vocabulary:", error);
    throw error;
  }
};

export const createVocabulary = async (data: Partial<Vocabulary>, adminUid: string): Promise<string> => {
  try {
    const word = data.word || "";
    const audioText = data.audioText || word;
    const audioUrl = data.audioUrl || "";
    const pronunciationSource = audioUrl ? "manual" : "tts";

    const docRef = await addDoc(collection(db, VOCABULARIES_COLLECTION), {
      ...data,
      audioText,
      pronunciationSource,
      status: data.status || "active",
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
      createdBy: adminUid,
      updatedBy: adminUid
    });

    await updateDoc(docRef, { vocabId: docRef.id });
    return docRef.id;
  } catch (error) {
    console.error("Error creating vocabulary:", error);
    throw error;
  }
};

export const updateVocabulary = async (vocabId: string, data: Partial<Vocabulary>, adminUid: string): Promise<void> => {
  try {
    const docRef = doc(db, VOCABULARIES_COLLECTION, vocabId);
    let updateData = { ...data };
    if (data.word && !data.audioText) updateData.audioText = data.word;
    if (data.audioUrl !== undefined) updateData.pronunciationSource = data.audioUrl ? "manual" : "tts";

    await updateDoc(docRef, {
      ...updateData,
      updatedAt: serverTimestamp(),
      updatedBy: adminUid
    });
  } catch (error) {
    console.error("Error updating vocabulary:", error);
    throw error;
  }
};

export const softDeleteVocabulary = async (vocabId: string, adminUid: string): Promise<void> => {
  try {
    const docRef = doc(db, VOCABULARIES_COLLECTION, vocabId);
    await updateDoc(docRef, {
      status: "deleted",
      updatedAt: serverTimestamp(),
      updatedBy: adminUid
    });
  } catch (error) {
    console.error("Error deleting vocabulary:", error);
    throw error;
  }
};

export const deleteVocabulariesBatch = async (vocabIds: string[], adminUid: string): Promise<void> => {
    const batch = (await import("firebase/firestore")).writeBatch(db);
    vocabIds.forEach(id => {
        batch.update(doc(db, VOCABULARIES_COLLECTION, id), {
            status: "deleted",
            updatedAt: serverTimestamp(),
            updatedBy: adminUid
        });
    });
    await batch.commit();
};

export const importVocabularyFull = async (rawData: any[], adminUid: string): Promise<void> => {
  try {
    const [topicsSnap, lessonsSnap, vocabSnap] = await Promise.all([
      getDocs(collection(db, TOPICS_COLLECTION)),
      getDocs(collection(db, LESSONS_COLLECTION)),
      getDocs(collection(db, VOCABULARIES_COLLECTION))
    ]);

    let existingTopics = topicsSnap.docs.map(d => ({ id: d.id, ...d.data() } as any));
    let existingLessons = lessonsSnap.docs.map(d => ({ id: d.id, ...d.data() } as any));
    let existingVocab = vocabSnap.docs.map(d => ({ id: d.id, ...d.data() } as any)).filter(v => v.status !== "deleted");

    let maxTopicOrder = existingTopics.length > 0 ? Math.max(...existingTopics.map((t: any) => t.order || 0)) : -1;

    for (const row of rawData) {
      const topicName = row.topicName.trim();
      const lessonName = row.lessonName.trim();
      const word = row.vocabData.word.trim();

      // 1. Xử lý Topic
      let topic = existingTopics.find(t => t.name.toLowerCase() === topicName.toLowerCase());
      let topicId = topic ? topic.id : "";
      if (!topic) {
        maxTopicOrder++;
        const topicRef = await addDoc(collection(db, TOPICS_COLLECTION), {
          name: topicName,
          description: "Tự động tạo từ import",
          order: maxTopicOrder,
          status: "active",
          createdAt: serverTimestamp(),
          updatedAt: serverTimestamp(),
          createdBy: adminUid,
          updatedBy: adminUid
        });
        topicId = topicRef.id;
        await updateDoc(topicRef, { topicId });
        existingTopics.push({ id: topicId, name: topicName, order: maxTopicOrder });
      }

      // 2. Xử lý Lesson
      let lesson = existingLessons.find(l => l.name.toLowerCase() === lessonName.toLowerCase() && l.topicId === topicId);
      let lessonId = lesson ? lesson.id : "";
      if (!lesson) {
        const lessonsInTopic = existingLessons.filter((l: any) => l.topicId === topicId);
        const maxLOrder = lessonsInTopic.length > 0 ? Math.max(...lessonsInTopic.map((l: any) => l.order || 0)) : -1;
        const newLOrder = maxLOrder + 1;
        const lessonRef = await addDoc(collection(db, LESSONS_COLLECTION), {
          topicId, name: lessonName, description: "Tự động tạo từ import",
          order: newLOrder, totalWords: 0, status: "active",
          createdAt: serverTimestamp(), updatedAt: serverTimestamp(),
          createdBy: adminUid, updatedBy: adminUid
        });
        lessonId = lessonRef.id;
        await updateDoc(lessonRef, { lessonId });
        existingLessons.push({ id: lessonId, name: lessonName, topicId, order: newLOrder });
      }

      // 3. Kiểm tra trùng từ vựng trong bài học
      const isDuplicate = existingVocab.some(v => v.lessonId === lessonId && v.word.toLowerCase() === word.toLowerCase());
      if (isDuplicate) continue;

      // 4. Tạo từ vựng
      const vocabRef = await addDoc(collection(db, VOCABULARIES_COLLECTION), {
        ...row.vocabData,
        lessonId,
        audioText: word,
        pronunciationSource: "tts",
        createdAt: serverTimestamp(),
        updatedAt: serverTimestamp(),
        createdBy: adminUid,
        updatedBy: adminUid
      });
      await updateDoc(vocabRef, { vocabId: vocabRef.id });
      existingVocab.push({ lessonId, word });
    }
  } catch (error) {
    console.error("Error in vocabulary import:", error);
    throw error;
  }
};
