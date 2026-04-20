import { collection, getDocs, query, where } from "firebase/firestore";
import { db } from "../../firebase/firebase";

export interface DashboardStats {
  totalUsers: number;
  totalTopics: number;
  totalLessons: number;
  totalVocabulary: number;
  totalQuestions: number;
  totalQuizResults: number;
}

export const getDashboardStats = async (): Promise<DashboardStats> => {
  try {
    const collections = [
      "users",
      "topics",
      "lessons",
      "vocabularies",
      "questions",
      "quiz_results",
    ];

    const results = await Promise.all(
      collections.map(async (colName) => {
        try {
          let colRef = collection(db, colName);

          // Apply filter for specific collections
          if (["topics", "lessons", "vocabularies", "questions"].includes(colName)) {
            // Note: In Firestore, query(where("status", "!=", "deleted"))
            // will only return documents that HAVE the "status" field.
            // Requirement says: "if status field is missing, still count it as active for now."
            // To fulfill this, we fetch all and filter in JS if the DB doesn't support "missing or not equal" easily without composite indexes/extra work.
            // For a dashboard count in early phases, this is acceptable.
            const snapshot = await getDocs(colRef);
            return snapshot.docs.filter(doc => doc.data().status !== "deleted").length;
          } else {
            // For users and quiz_results, count all
            const snapshot = await getDocs(colRef);
            return snapshot.size;
          }
        } catch (error) {
          console.warn(`Collection ${colName} not found or inaccessible:`, error);
          return 0;
        }
      })
    );

    return {
      totalUsers: results[0],
      totalTopics: results[1],
      totalLessons: results[2],
      totalVocabulary: results[3],
      totalQuestions: results[4],
      totalQuizResults: results[5],
    };
  } catch (error) {
    console.error("Error fetching dashboard stats:", error);
    throw error;
  }
};
