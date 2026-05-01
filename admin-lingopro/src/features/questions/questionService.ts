import {
  collection,
  getDocs,
  addDoc,
  updateDoc,
  doc,
  serverTimestamp,
  query,
  orderBy,
  writeBatch,
  where
} from "firebase/firestore";
import { db } from "../../firebase/firebase";
import type { Question } from "../../models/Question";

const QUESTIONS_COLLECTION = "questions";
const TOPICS_COLLECTION = "topics";
const LESSONS_COLLECTION = "lessons";

export const getQuestions = async (): Promise<Question[]> => {
  try {
    const q = query(
      collection(db, QUESTIONS_COLLECTION),
      orderBy("createdAt", "desc")
    );

    const snapshot = await getDocs(q);
    return snapshot.docs
      .map(d => ({ ...d.data(), questionId: d.id } as Question))
      .filter(q => q.status !== "deleted");
  } catch (error) {
    console.error("Error fetching questions:", error);
    throw error;
  }
};

export const createQuestion = async (data: Partial<Question>, adminUid: string): Promise<string> => {
  try {
    const docRef = await addDoc(collection(db, QUESTIONS_COLLECTION), {
      ...data,
      status: data.status || "active",
      usage: data.usage || "both",
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
      createdBy: adminUid,
      updatedBy: adminUid
    });

    await updateDoc(docRef, { questionId: docRef.id });
    return docRef.id;
  } catch (error) {
    console.error("Error creating question:", error);
    throw error;
  }
};

export const updateQuestion = async (questionId: string, data: Partial<Question>, adminUid: string): Promise<void> => {
  try {
    const docRef = doc(db, QUESTIONS_COLLECTION, questionId);
    await updateDoc(docRef, {
      ...data,
      updatedAt: serverTimestamp(),
      updatedBy: adminUid
    });
  } catch (error) {
    console.error("Error updating question:", error);
    throw error;
  }
};

export const softDeleteQuestion = async (questionId: string, adminUid: string): Promise<void> => {
  try {
    const docRef = doc(db, QUESTIONS_COLLECTION, questionId);
    await updateDoc(docRef, {
      status: "deleted",
      updatedAt: serverTimestamp(),
      updatedBy: adminUid
    });
  } catch (error) {
    console.error("Error deleting question:", error);
    throw error;
  }
};

export const deleteQuestionsBatch = async (questionIds: string[], adminUid: string): Promise<void> => {
  try {
    const batch = writeBatch(db);
    questionIds.forEach(id => {
      const docRef = doc(db, QUESTIONS_COLLECTION, id);
      batch.update(docRef, {
        status: "deleted",
        updatedAt: serverTimestamp(),
        updatedBy: adminUid
      });
    });
    await batch.commit();
  } catch (error) {
    console.error("Error in deleteQuestionsBatch:", error);
    throw error;
  }
};

export const importQuestionsFull = async (rawData: any[], adminUid: string): Promise<void> => {
  try {
    // 1. Lấy toàn bộ Topics, Lessons và Questions hiện có để so sánh
    const [topicsSnap, lessonsSnap, questionsSnap] = await Promise.all([
      getDocs(collection(db, TOPICS_COLLECTION)),
      getDocs(collection(db, LESSONS_COLLECTION)),
      getDocs(collection(db, QUESTIONS_COLLECTION))
    ]);

    let existingTopics = topicsSnap.docs.map(d => ({ id: d.id, ...d.data() } as any));
    let existingLessons = lessonsSnap.docs.map(d => ({ id: d.id, ...d.data() } as any));
    let existingQuestions = questionsSnap.docs
      .map(d => ({ id: d.id, ...d.data() } as any))
      .filter(q => q.status !== "deleted");

    let maxTopicOrder = existingTopics.length > 0 ? Math.max(...existingTopics.map((t: any) => t.order || 0)) : -1;

    for (const row of rawData) {
      const topicName = row.topicName.trim();
      const lessonName = row.lessonName.trim();
      const questionText = row.questionData.questionText.trim();

      // 2. Xử lý Topic
      let topic = existingTopics.find(t => t.name.toLowerCase() === topicName.toLowerCase());
      let topicId = "";

      if (topic) {
        topicId = topic.id;
      } else {
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

      // 3. Xử lý Lesson
      let lesson = existingLessons.find(l =>
        l.name.toLowerCase() === lessonName.toLowerCase() &&
        l.topicId === topicId
      );
      let lessonId = "";

      if (lesson) {
        lessonId = lesson.id;
      } else {
        const lessonsInThisTopic = existingLessons.filter((l: any) => l.topicId === topicId);
        const maxLessonOrder = lessonsInThisTopic.length > 0 ? Math.max(...lessonsInThisTopic.map((l: any) => l.order || 0)) : -1;
        const newLessonOrder = maxLessonOrder + 1;

        const lessonRef = await addDoc(collection(db, LESSONS_COLLECTION), {
          topicId,
          name: lessonName,
          description: "Tự động tạo từ import",
          order: newLessonOrder,
          totalWords: 0,
          status: "active",
          createdAt: serverTimestamp(),
          updatedAt: serverTimestamp(),
          createdBy: adminUid,
          updatedBy: adminUid
        });
        lessonId = lessonRef.id;
        await updateDoc(lessonRef, { lessonId });
        existingLessons.push({ id: lessonId, name: lessonName, topicId: topicId, order: newLessonOrder });
      }

      // 4. Kiểm tra trùng câu hỏi (Tránh thêm cùng 1 câu vào 1 bài học)
      const isDuplicate = existingQuestions.some(q =>
        q.lessonId === lessonId &&
        q.questionText.trim().toLowerCase() === questionText.toLowerCase()
      );

      if (isDuplicate) {
        console.log(`Bỏ qua câu hỏi trùng: "${questionText}" trong bài học "${lessonName}"`);
        continue; // Bỏ qua dòng này, chuyển sang dòng tiếp theo trong Excel
      }

      // 5. Tạo câu hỏi mới
      const questionRef = await addDoc(collection(db, QUESTIONS_COLLECTION), {
        ...row.questionData,
        lessonId,
        createdAt: serverTimestamp(),
        updatedAt: serverTimestamp(),
        createdBy: adminUid,
        updatedBy: adminUid
      });
      await updateDoc(questionRef, { questionId: questionRef.id });

      // Cập nhật mảng local để tránh trùng ngay trong cùng 1 file Excel
      existingQuestions.push({
        lessonId,
        questionText: questionText
      });
    }
  } catch (error) {
    console.error("Error in full import:", error);
    throw error;
  }
};
