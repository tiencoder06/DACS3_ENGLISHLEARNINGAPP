import {
  collection,
  getDocs,
  addDoc,
  updateDoc,
  doc,
  serverTimestamp,
  query,
  where,
  orderBy
} from "firebase/firestore";
import { db } from "../../firebase/firebase";
import { type Topic } from "../../models/Topic";

const TOPICS_COLLECTION = "topics";

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

export const softDeleteTopic = async (topicId: string, adminUid: string): Promise<void> => {
  try {
    const docRef = doc(db, TOPICS_COLLECTION, topicId);
    await updateDoc(docRef, {
      status: "deleted",
      updatedAt: serverTimestamp(),
      updatedBy: adminUid
    });
  } catch (error) {
    console.error("Error deleting topic:", error);
    throw error;
  }
};
