export interface PronunciationData {
  pronunciation: string;
  audioUrl: string;
  partOfSpeech: string;
  audioText: string;
  pronunciationSource: "dictionary_api" | "tts";
}

export const fetchPronunciation = async (word: string): Promise<PronunciationData> => {
  if (!word.trim()) {
    throw new Error("Vui lòng nhập từ vựng");
  }

  try {
    const response = await fetch(`https://api.dictionaryapi.dev/api/v2/entries/en/${encodeURIComponent(word.trim())}`);

    if (!response.ok) {
      if (response.status === 404) {
        throw new Error("Không tìm thấy từ này trong từ điển");
      }
      throw new Error("Lỗi khi kết nối với máy chủ từ điển");
    }

    const data = await response.json();
    const entry = data[0];

    let pronunciation = "";
    let audioUrl = "";
    let partOfSpeech = "";

    // Lấy phiên âm và audio từ mảng phonetics
    if (entry.phonetics && entry.phonetics.length > 0) {
      // Tìm phonetic đầu tiên có audio
      const audioPhonetic = entry.phonetics.find((p: any) => p.audio && p.audio !== "");
      if (audioPhonetic) {
        audioUrl = audioPhonetic.audio;
        pronunciation = audioPhonetic.text || entry.phonetic || "";
      } else {
        // Fallback lấy phonetic đầu tiên
        pronunciation = entry.phonetics[0].text || entry.phonetic || "";
      }
    } else {
      pronunciation = entry.phonetic || "";
    }

    // Chuẩn hóa audioUrl
    if (audioUrl.startsWith("//")) {
      audioUrl = `https:${audioUrl}`;
    }

    // Lấy loại từ
    if (entry.meanings && entry.meanings.length > 0) {
      partOfSpeech = entry.meanings[0].partOfSpeech || "";
    }

    return {
      pronunciation,
      audioUrl,
      partOfSpeech,
      audioText: word.trim(),
      pronunciationSource: audioUrl ? "dictionary_api" : "tts"
    };
  } catch (error: any) {
    console.error("fetchPronunciation error:", error);
    throw error;
  }
};

export const previewAudio = (audioUrl?: string, audioText?: string) => {
  if (audioUrl) {
    const audio = new Audio(audioUrl);
    audio.play().catch(err => {
      console.error("Audio playback failed, falling back to TTS:", err);
      speakTTS(audioText || "");
    });
  } else if (audioText) {
    speakTTS(audioText);
  }
};

const speakTTS = (text: string) => {
  if (!window.speechSynthesis) return;

  // Hủy các yêu cầu phát âm trước đó
  window.speechSynthesis.cancel();

  const utterance = new SpeechSynthesisUtterance(text);
  utterance.lang = "en-US";
  window.speechSynthesis.speak(utterance);
};
