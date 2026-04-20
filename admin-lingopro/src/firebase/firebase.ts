import { initializeApp } from "firebase/app";
import { getAuth } from "firebase/auth";
import { getFirestore } from "firebase/firestore";

// Dán cấu hình Firebase Web của bạn vào đây
const firebaseConfig = {
  apiKey: "AIzaSyAicSYCJ0r0mzc7KKGj4Tat8TEmfH-RBW0",
  authDomain: "englishlearningapp-1abf4.firebaseapp.com",
  projectId: "englishlearningapp-1abf4",
  storageBucket: "englishlearningapp-1abf4.firebasestorage.app",
  messagingSenderId: "633502310660",
  appId: "1:633502310660:web:8861aa5d2e1e22e07a7051"
};

const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);
export const db = getFirestore(app);
