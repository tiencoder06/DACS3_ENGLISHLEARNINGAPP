import { Timestamp } from "firebase/firestore";

export type LessonStatus = "active" | "inactive" | "deleted";

export interface Lesson {
  lessonId: string;
  topicId: string;
  name: string;
  description: string;
  order: number;
  totalWords: number;
  status: LessonStatus;
  createdAt: Timestamp;
  updatedAt: Timestamp;
  createdBy: string;
  updatedBy: string;
}
