import { Timestamp } from "firebase/firestore";

export type VocabularyStatus = "active" | "inactive" | "deleted";
export type PronunciationSource = "manual" | "dictionary_api" | "tts";

export interface Vocabulary {
  vocabId: string;
  lessonId: string;
  word: string;
  meaning: string;
  pronunciation: string;
  partOfSpeech: string;
  exampleSentence: string;
  audioText: string;
  audioUrl: string;
  pronunciationSource: PronunciationSource;
  status: VocabularyStatus;
  createdAt: Timestamp;
  updatedAt: Timestamp;
  createdBy: string;
  updatedBy: string;
}
