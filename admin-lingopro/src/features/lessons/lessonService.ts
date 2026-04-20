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
import type { Lesson } from "../../models/Lesson";

const LESSONS_COLLECTION = "lessons";

export const getLessons = async (): Promise<Lesson[]> => {
  try {
    const q = query(
      collection(db, LESSONS_COLLECTION),
      orderBy("order", "asc")
    );

    const snapshot = await getDocs(q);
    return snapshot.docs
      .map(d => ({ ...d.data(), lessonId: d.id } as Lesson))
      .filter(lesson => lesson.status !== "deleted");
  } catch (error) {
    console.error("Error fetching lessons:", error);
    throw error;
  }
};

export const createLesson = async (data: Partial<Lesson>, adminUid: string): Promise<string> => {
  try {
    const docRef = await addDoc(collection(db, LESSONS_COLLECTION), {
      ...data,
      totalWords: data.totalWords || 0,
      status: data.status || "active",
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
      createdBy: adminUid,
      updatedBy: adminUid
    });

    // Update lessonId inside the document
    await updateDoc(docRef, { lessonId: docRef.id });
    return docRef.id;
  } catch (error) {
    console.error("Error creating lesson:", error);
    throw error;
  }
};

export const updateLesson = async (lessonId: string, data: Partial<Lesson>, adminUid: string): Promise<void> => {
  try {
    const docRef = doc(db, LESSONS_COLLECTION, lessonId);
    await updateDoc(docRef, {
      ...data,
      updatedAt: serverTimestamp(),
      updatedBy: adminUid
    });
  } catch (error) {
    console.error("Error updating lesson:", error);
    throw error;
  }
};

export const softDeleteLesson = async (lessonId: string, adminUid: string): Promise<void> => {
  try {
    const docRef = doc(db, LESSONS_COLLECTION, lessonId);
    await updateDoc(docRef, {
      status: "deleted",
      updatedAt: serverTimestamp(),
      updatedBy: adminUid
    });
  } catch (error) {
    console.error("Error deleting lesson:", error);
    throw error;
  }
};
