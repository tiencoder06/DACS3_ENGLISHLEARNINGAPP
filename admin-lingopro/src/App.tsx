import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import ProtectedRoute from "./components/ProtectedRoute";
import AdminLayout from "./layouts/AdminLayout";
import Login from "./pages/Login";
import DashboardPage from "./pages/DashboardPage";
import TopicsPage from "./features/topics/TopicsPage";
// Import trang Lessons thật vừa mới triển khai
import LessonsPage from "./features/lessons/LessonsPage";
import {
  VocabularyPage,
  QuestionsPage,
  ImportPage,
  UsersPage,
  ResultsPage
} from "./pages/PlaceholderPages";

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          {/* Public Route */}
          <Route path="/login" element={<Login />} />

          {/* Protected Admin Routes */}
          <Route
            path="/"
            element={
              <ProtectedRoute>
                <AdminLayout />
              </ProtectedRoute>
            }
          >
            <Route index element={<Navigate to="/dashboard" replace />} />
            <Route path="dashboard" element={<DashboardPage />} />
            <Route path="topics" element={<TopicsPage />} />
            <Route path="lessons" element={<LessonsPage />} />
            <Route path="vocabulary" element={<VocabularyPage />} />
            <Route path="questions" element={<QuestionsPage />} />
            <Route path="import" element={<ImportPage />} />
            <Route path="users" element={<UsersPage />} />
            <Route path="results" element={<ResultsPage />} />
          </Route>

          {/* Fallback */}
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
