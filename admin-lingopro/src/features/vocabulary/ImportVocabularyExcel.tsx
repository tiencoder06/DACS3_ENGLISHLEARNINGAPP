import React, { useRef, useState } from "react";
import * as XLSX from "xlsx";

interface ImportVocabularyExcelProps {
  onImport: (rawData: any[]) => Promise<void>;
}

const ImportVocabularyExcel: React.FC<ImportVocabularyExcelProps> = ({ onImport }) => {
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

        const formattedData = data.map((row) => {
          const getVal = (target: string) => {
            const key = Object.keys(row).find(k => k.trim().toLowerCase() === target.toLowerCase());
            return key ? row[key] : null;
          };

          const topicName = getVal("TopicName")?.toString().trim();
          const lessonName = getVal("LessonName")?.toString().trim();
          const word = getVal("Word")?.toString().trim();
          const meaning = getVal("Meaning")?.toString().trim();
          const pronunciation = getVal("Pronunciation")?.toString().trim();
          const example = getVal("Example")?.toString().trim();

          if (!topicName || !lessonName || !word || !meaning) return null;

          return {
            topicName,
            lessonName,
            vocabData: {
              word,
              meaning,
              pronunciation: pronunciation || "",
              exampleSentence: example || "",
              status: "active"
            }
          };
        }).filter(item => item !== null);

        if (formattedData.length === 0) {
          alert("Không tìm thấy dữ liệu hợp lệ.");
        } else {
          if (window.confirm(`Hệ thống sẽ tự động tạo Chủ đề và Bài học nếu chưa có. Tiếp tục nhập ${formattedData.length} từ vựng?`)) {
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
        Word: "Hello",
        Meaning: "Xin chào",
        Pronunciation: "/həˈloʊ/",
        Example: "Hello, how are you?"
      }
    ];
    const ws = XLSX.utils.json_to_sheet(template);
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, "Template");
    XLSX.writeFile(wb, "Mau_Tu_Vung.xlsx");
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
        Nhập từ Excel
      </button>
      <button onClick={downloadTemplate} className="p-2.5 text-green-600 border border-green-600 rounded-xl hover:bg-green-50 transition" title="Tải file mẫu">
        📥
      </button>
    </div>
  );
};

export default ImportVocabularyExcel;
