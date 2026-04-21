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
    const response = await fetch(
      `https://api.dictionaryapi.dev/api/v2/entries/en/${encodeURIComponent(word.trim())}`
    );

    if (!response.ok) {
      if (response.status === 404) {
        throw new Error("Không tìm thấy từ này trong từ điển");
      }
      throw new Error("Lỗi khi kết nối với máy chủ từ điển");
    }

    const data = await response.json();
    if (!Array.isArray(data) || data.length === 0) {
      throw new Error("Dữ liệu từ điển không hợp lệ");
    }

    const entry = data[0];
    let pronunciation = "";
    let audioUrl = "";
    let partOfSpeech = "";

    // Extract pronunciation and audio
    if (entry.phonetics && Array.isArray(entry.phonetics)) {
      // Find the first item with audio
      const audioPhonetic = entry.phonetics.find(
        (p: any) => p.audio && p.audio.trim() !== ""
      );

      if (audioPhonetic) {
        audioUrl = audioPhonetic.audio;
        // Normalize audioUrl
        if (audioUrl.startsWith("//")) {
          audioUrl = `https:${audioUrl}`;
        }
        pronunciation = audioPhonetic.text || entry.phonetic || "";
      } else {
        // Fallback to first phonetic item with text
        const textPhonetic = entry.phonetics.find((p: any) => p.text && p.text.trim() !== "");
        pronunciation = textPhonetic?.text || entry.phonetic || "";
      }
    } else {
      pronunciation = entry.phonetic || "";
    }

    // Extract partOfSpeech from meanings
    if (entry.meanings && Array.isArray(entry.meanings) && entry.meanings.length > 0) {
      partOfSpeech = entry.meanings[0].partOfSpeech || "";
    }

    return {
      pronunciation,
      audioUrl,
      partOfSpeech,
      audioText: word.trim(),
      pronunciationSource: audioUrl ? "dictionary_api" : "tts",
    };
  } catch (error: any) {
    console.error("fetchPronunciation error:", error);
    throw error;
  }
};

export const previewAudio = (audioUrl?: string, audioText?: string) => {
  if (audioUrl && audioUrl.trim() !== "") {
    const audio = new Audio(audioUrl);
    audio.play().catch((err) => {
      console.error("Audio playback failed, falling back to TTS:", err);
      if (audioText) speakTTS(audioText);
    });
  } else if (audioText && audioText.trim() !== "") {
    speakTTS(audioText);
  }
};

const speakTTS = (text: string) => {
  if (!window.speechSynthesis) {
    console.warn("SpeechSynthesis not supported in this browser");
    return;
  }

  // Cancel any ongoing speech
  window.speechSynthesis.cancel();

  const utterance = new SpeechSynthesisUtterance(text);
  utterance.lang = "en-US";
  window.speechSynthesis.speak(utterance);
};
