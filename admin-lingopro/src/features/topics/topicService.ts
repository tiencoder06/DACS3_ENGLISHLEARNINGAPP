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
import { type Topic } from "../../models/Topic";

const TOPICS_COLLECTION = "topics";
const LESSONS_COLLECTION = "lessons";
const VOCABULARIES_COLLECTION = "vocabularies";
const QUESTIONS_COLLECTION = "questions";

export const getTopics = async (): Promise<Topic[]> => {
  try {
    const q = query(
      collection(db, TOPICS_COLLECTION),
      orderBy("order", "asc")
    );

    const snapshot = await getDocs(q);
    return snapshot.docs
      .map(d => ({ ...d.data(), topicId: d.id } as Topic))
      .filter(topic => topic.status !== "deleted");
  } catch (error) {
    console.error("Error fetching topics:", error);
    throw error;
  }
};

export const createTopic = async (data: Partial<Topic>, adminUid: string): Promise<string> => {
  try {
    const docRef = await addDoc(collection(db, TOPICS_COLLECTION), {
      ...data,
      status: data.status || "active",
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
      createdBy: adminUid,
      updatedBy: adminUid
    });

    // Update topicId inside the document
    await updateDoc(docRef, { topicId: docRef.id });
    return docRef.id;
  } catch (error) {
    console.error("Error creating topic:", error);
    throw error;
  }
};

export const updateTopic = async (topicId: string, data: Partial<Topic>, adminUid: string): Promise<void> => {
  try {
    const docRef = doc(db, TOPICS_COLLECTION, topicId);
    await updateDoc(docRef, {
      ...data,
      updatedAt: serverTimestamp(),
      updatedBy: adminUid
    });
  } catch (error) {
    console.error("Error updating topic:", error);
    throw error;
  }
};

/**
 * Xóa Topic và tất cả Lesson, Vocabulary, Question thuộc Topic đó
 */
export const softDeleteTopic = async (topicId: string, adminUid: string): Promise<void> => {
  try {
    const batch = writeBatch(db);

    // 1. Đánh dấu Topic là đã xóa
    const topicRef = doc(db, TOPICS_COLLECTION, topicId);
    batch.update(topicRef, {
      status: "deleted",
      updatedAt: serverTimestamp(),
      updatedBy: adminUid
    });

    // 2. Lấy tất cả Lesson thuộc Topic này
    const lessonsSnap = await getDocs(query(
        collection(db, LESSONS_COLLECTION),
        where("topicId", "==", topicId)
    ));

    for (const lessonDoc of lessonsSnap.docs) {
      const lessonId = lessonDoc.id;

      // Đánh dấu Lesson là đã xóa
      batch.update(lessonDoc.ref, {
        status: "deleted",
        updatedAt: serverTimestamp(),
        updatedBy: adminUid
      });

      // Tìm và đánh dấu Từ vựng thuộc Lesson này
      const vocabSnap = await getDocs(query(
          collection(db, VOCABULARIES_COLLECTION),
          where("lessonId", "==", lessonId)
      ));
      vocabSnap.forEach(d => {
        batch.update(d.ref, {
          status: "deleted",
          updatedAt: serverTimestamp(),
          updatedBy: adminUid
        });
      });

      // Tìm và đánh dấu Câu hỏi thuộc Lesson này
      const questionsSnap = await getDocs(query(
          collection(db, QUESTIONS_COLLECTION),
          where("lessonId", "==", lessonId)
      ));
      questionsSnap.forEach(d => {
        batch.update(d.ref, {
          status: "deleted",
          updatedAt: serverTimestamp(),
          updatedBy: adminUid
        });
      });
    }

    await batch.commit();
  } catch (error) {
    console.error("Error deleting topic and its cascading contents:", error);
    throw error;
  }
};

export const deleteTopicsBatch = async (topicIds: string[], adminUid: string): Promise<void> => {
  try {
    // Để đơn giản và đảm bảo cascade, chúng ta gọi softDeleteTopic cho từng cái
    // (Vì mỗi cái cần query con của nó)
    for (const id of topicIds) {
        await softDeleteTopic(id, adminUid);
    }
  } catch (error) {
    console.error("Error in deleteTopicsBatch:", error);
    throw error;
  }
};
