import {
  collection,
  getDocs,
  addDoc,
  updateDoc,
  doc,
  serverTimestamp,
  query,
  orderBy
} from "firebase/firestore";
import { db } from "../../firebase/firebase";
import type { Question } from "../../models/Question";

const QUESTIONS_COLLECTION = "questions";

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
