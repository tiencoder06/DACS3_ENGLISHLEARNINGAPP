import React, { createContext, useContext, useEffect, useState } from "react";
import { onAuthStateChanged, type User, signInWithEmailAndPassword, signOut } from "firebase/auth";
import { doc, getDoc } from "firebase/firestore";
import { auth, db } from "../firebase/firebase";

interface UserProfile {
  uid: string;
  email: string;
  fullName: string;
  role: "admin" | "user";
}

interface AuthContextType {
  currentUser: User | null;
  userProfile: UserProfile | null;
  isAdmin: boolean;
  loading: boolean;
  login: (email: string, pass: string) => Promise<void>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [currentUser, setCurrentUser] = useState<User | null>(null);
  const [userProfile, setUserProfile] = useState<UserProfile | null>(null);
  const [isAdmin, setIsAdmin] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
      const unsubscribe = onAuthStateChanged(auth, async (user) => {
        if (user) {
          setLoading(true); // Bật trạng thái Loading trước khi đi lấy dữ liệu
          setCurrentUser(user);
          try {
            const userDoc = await getDoc(doc(db, "users", user.uid));
            if (userDoc.exists()) {
              const profile = userDoc.data() as UserProfile;
              setUserProfile(profile);
              setIsAdmin(profile.role === "admin");
            }
          } catch (error) {
            console.error("Lỗi khi tải thông tin user:", error);
          } finally {
            setLoading(false); // Lấy xong rồi thì tắt Loading
          }
        } else {
          setCurrentUser(null);
          setUserProfile(null);
          setIsAdmin(false);
          setLoading(false);
        }
      });

      return unsubscribe;
    }, []);

  const login = async (email: string, pass: string) => {
    await signInWithEmailAndPassword(auth, email, pass);
  };

  const logout = async () => {
    await signOut(auth);
  };

  return (
    <AuthContext.Provider value={{ currentUser, userProfile, isAdmin, loading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
};