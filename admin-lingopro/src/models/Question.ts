import { Timestamp } from "firebase/firestore";

export type QuestionStatus = "active" | "inactive" | "deleted";
export type QuestionType = "multiple_choice" | "fill_blank" | "listen_choose";
export type QuestionUsage = "practice" | "quiz" | "both";

export interface Question {
  questionId: string;
  topicId: string;
  lessonId: string;
  vocabId: string;
  questionType: QuestionType;
  usage: QuestionUsage;
  title?: string; // Thêm tiêu đề câu hỏi (Ví dụ: Nghe và chọn đáp án đúng)
  questionText: string;
  options: string[];
  correctAnswer: string;
  explanation: string;
  status: QuestionStatus;
  createdAt: Timestamp;
  updatedAt: Timestamp;
  createdBy: string;
  updatedBy: string;
}
