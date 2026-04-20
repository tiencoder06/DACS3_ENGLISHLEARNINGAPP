import { Timestamp } from "firebase/firestore";

export type TopicStatus = "active" | "inactive" | "deleted";

export interface Topic {
  topicId: string;
  name: string;
  description: string;
  order: number;
  status: TopicStatus;
  createdAt: Timestamp;
  updatedAt: Timestamp;
  createdBy: string;
  updatedBy: string;
}
