# LingoPro AI Proxy (Cloudflare Worker)

Dự án này đóng vai trò là một Proxy bảo mật để kết nối ứng dụng EnglishLearningApp với Gemini AI API.

## Mục tiêu
- Bảo mật Gemini API Key (không để ở Client Android).
- Kiểm soát dữ liệu và định dạng phản hồi (System Prompt).
- Tiết kiệm chi phí (Cloudflare Free Tier hỗ trợ 100k requests/ngày).

## Cấu trúc dự án
- `src/index.ts`: Mã nguồn chính xử lý request và gọi AI.
- `wrangler.toml`: File cấu hình Cloudflare Worker.
- `.dev.vars.example`: File mẫu chứa các biến môi trường nhạy cảm.

## Hướng dẫn cài đặt và chạy Local

### 1. Cài đặt Dependencies
Mở terminal tại thư mục này và chạy:
```bash
npm install
```

### 2. Cấu hình biến môi trường
Copy file mẫu thành file thật (wrangler sẽ tự đọc file này khi chạy dev):
```bash
cp .dev.vars.example .dev.vars
```
Sau đó mở `.dev.vars` và điền API Key của bạn (lấy từ https://aistudio.google.com/). *Lưu ý: API Key chưa thực sự được dùng ở Phase 3B.1 này.*

### 3. Chạy môi trường Development
```bash
npx wrangler dev
```
Worker sẽ chạy tại địa chỉ mặc định là: `http://localhost:8787`

### 4. Kiểm tra Endpoint (Testing)
Sử dụng cURL hoặc Postman để gửi yêu cầu thử nghiệm:

```bash
curl -X POST http://localhost:8787/api/v1/chat \
     -H "Content-Type: application/json" \
     -d '{
       "userMessage": "Giải thích thì hiện tại đơn",
       "placementLevel": "Beginner",
       "placementWeakSkill": "Listening",
       "recentMessages": []
     }'
```

**Kết quả mong đợi (Mock JSON):**
```json
{
  "success": true,
  "assistantMessage": "...",
  "errorMessage": null
}
```

## Các Phase tiếp theo
- **Phase 3B.2:** Tích hợp Gemini SDK thật vào Worker.
- **Phase 3B.3:** Android gọi Worker này thay vì dùng Mock code cũ.
- **Phase 3B.4:** Thêm logic bảo mật Verify Firebase ID Token và Rate Limit.
