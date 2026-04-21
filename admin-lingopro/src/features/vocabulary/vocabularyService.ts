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
import type { Vocabulary } from "../../models/Vocabulary";

const VOCABULARIES_COLLECTION = "vocabularies";

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

    // Determine pronunciation source
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
    if (data.word && !data.audioText) {
        updateData.audioText = data.word;
    }

    if (data.audioUrl !== undefined) {
        updateData.pronunciationSource = data.audioUrl ? "manual" : "tts";
    }

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
