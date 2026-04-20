import { Timestamp } from "firebase/firestore";

export type LessonStatus = "active" | "inactive" | "deleted";

export interface Lesson {
  lessonId: string;
  topicId: string;
  topicName?: string; // Tên topic để hiển thị trong bảng
  title: string;
  description: string;
  content: string;
  order: number;
  status: LessonStatus;
  createdAt: Timestamp;
  updatedAt: Timestamp;
  createdBy: string;
  updatedBy: string;
}
