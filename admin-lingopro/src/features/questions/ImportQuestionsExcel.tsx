import React, { useRef, useState } from "react";
import * as XLSX from "xlsx";
import { type QuestionType, type QuestionUsage } from "../../models/Question";

interface ImportQuestionsExcelProps {
  onImport: (rawData: any[]) => Promise<void>;
}

const ImportQuestionsExcel: React.FC<ImportQuestionsExcelProps> = ({ onImport }) => {
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [importing, setImporting] = useState(false);

  const handleFileUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setImporting(true);
    const reader = new FileReader();
    reader.onload = async (evt) => {
      try {
        const bstr = evt.target?.result;
        const wb = XLSX.read(bstr, { type: "binary" });
        const wsname = wb.SheetNames[0];
        const ws = wb.Sheets[wsname];
        const data = XLSX.utils.sheet_to_json(ws) as any[];

        if (data.length === 0) {
          alert("File Excel không có dữ liệu.");
          return;
        }

        const formattedData = data.map((row) => {
          const getVal = (target: string) => {
            const key = Object.keys(row).find(k => k.trim().toLowerCase() === target.toLowerCase());
            return key ? row[key] : null;
          };

          const topicName = getVal("TopicName")?.toString().trim();
          const lessonName = getVal("LessonName")?.toString().trim();
          const vocabWord = getVal("VocabWord")?.toString().trim();
          const type = getVal("Type")?.toString().trim().toLowerCase();
          const usage = getVal("Usage")?.toString().trim().toLowerCase();
          const qText = getVal("QuestionText")?.toString().trim();
          const optionsStr = getVal("Options")?.toString().trim();
          const correct = getVal("CorrectAnswer")?.toString().trim();
          const explanation = getVal("Explanation")?.toString().trim();

          if (!topicName || !lessonName || !qText || !correct) return null;

          const options = optionsStr ? optionsStr.split("|").map((o: string) => o.trim()) : [];

          return {
            topicName,
            lessonName,
            questionData: {
              vocabId: "", // Sẽ xử lý liên kết vocab sau nếu cần, tạm thời để trống
              questionType: (type || "multiple_choice") as QuestionType,
              usage: (usage || "both") as QuestionUsage,
              questionText: qText,
              options: options,
              correctAnswer: correct,
              explanation: explanation || "",
              status: "active"
            }
          };
        }).filter(item => item !== null);

        if (formattedData.length === 0) {
          alert("Không tìm thấy dữ liệu hợp lệ (Thiếu TopicName, LessonName hoặc QuestionText).");
        } else {
          if (window.confirm(`Hệ thống sẽ tự động tạo Chủ đề và Bài học nếu chưa có. Tiếp tục nhập ${formattedData.length} câu hỏi?`)) {
            await onImport(formattedData);
            alert("Đã nhập dữ liệu thành công!");
          }
        }
      } catch (err) {
        console.error("Lỗi Excel:", err);
        alert("Có lỗi xảy ra khi đọc file.");
      } finally {
        setImporting(false);
        if (fileInputRef.current) fileInputRef.current.value = "";
      }
    };
    reader.readAsBinaryString(file);
  };

  const downloadTemplate = () => {
    const template = [
      {
        TopicName: "Giao tiếp",
        LessonName: "Chào hỏi",
        VocabWord: "Hello",
        Type: "multiple_choice",
        Usage: "both",
        QuestionText: "Nghĩa của từ 'Hello' là gì?",
        Options: "Xin chào|Tạm biệt|Cảm ơn|Xin lỗi",
        CorrectAnswer: "Xin chào",
        Explanation: "Hello dùng để chào hỏi cơ bản."
      }
    ];
    const ws = XLSX.utils.json_to_sheet(template);
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, "Template");
    XLSX.writeFile(wb, "Mau_Cau_Hoi_Auto_Create.xlsx");
  };

  return (
    <div className="flex items-center gap-2">
      <input type="file" ref={fileInputRef} onChange={handleFileUpload} accept=".xlsx, .xls" className="hidden" />
      <button
        onClick={() => fileInputRef.current?.click()}
        disabled={importing}
        className="px-4 py-2.5 bg-green-600 text-white rounded-xl font-bold hover:bg-green-700 transition shadow-lg disabled:opacity-50 flex items-center gap-2"
      >
        <span>{importing ? "⌛" : "📊"}</span>
        Nhập từ Excel (Auto Create)
      </button>
      <button onClick={downloadTemplate} className="p-2.5 text-green-600 border border-green-600 rounded-xl hover:bg-green-50 transition" title="Tải file mẫu">
        📥
      </button>
    </div>
  );
};

export default ImportQuestionsExcel;
