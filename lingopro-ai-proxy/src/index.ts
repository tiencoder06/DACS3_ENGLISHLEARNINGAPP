export interface Env {
	// GEMINI_API_KEY: string;
}

export default {
	async fetch(request: Request, env: Env, ctx: ExecutionContext): Promise<Response> {
		const url = new URL(request.url);

		// 1. Handle CORS Preflight
		if (request.method === "OPTIONS") {
			return new Response(null, {
				headers: {
					"Access-Control-Allow-Origin": "*",
					"Access-Control-Allow-Methods": "POST, OPTIONS",
					"Access-Control-Allow-Headers": "Content-Type, Authorization",
				},
			});
		}

		// 2. Route Validation
		if (url.pathname !== "/api/v1/chat") {
			return new Response(JSON.stringify({ success: false, errorMessage: "Not Found" }), {
				status: 404,
				headers: { "Content-Type": "application/json" },
			});
		}

		// 3. Method Validation
		if (request.method !== "POST") {
			return new Response(JSON.stringify({ success: false, errorMessage: "Method Not Allowed" }), {
				status: 405,
				headers: { "Content-Type": "application/json", "Allow": "POST" },
			});
		}

		try {
			// 4. Request Body Validation
			const body: any = await request.json();
			const { userMessage, placementLevel, placementWeakSkill, recentMessages } = body;

			if (!userMessage || typeof userMessage !== "string" || userMessage.trim().length === 0) {
				return new Response(JSON.stringify({ success: false, errorMessage: "userMessage is required" }), {
					status: 400,
					headers: { "Content-Type": "application/json" },
				});
			}

			if (userMessage.length > 200) {
				return new Response(JSON.stringify({ success: false, errorMessage: "userMessage is too long (max 200 chars)" }), {
					status: 400,
					headers: { "Content-Type": "application/json" },
				});
			}

			// 5. Mock AI Response (Phase 3B.1)
			const mockAssistantMessage = `Đây là phản hồi mock từ Cloudflare Worker. Mình đã nhận được tin nhắn: "${userMessage}". Ở phase sau, mình sẽ được kết nối với Gemini thật để tư vấn dựa trên trình độ ${placementLevel || 'Beginner'} của bạn.`;

			return new Response(
				JSON.stringify({
					success: true,
					assistantMessage: mockAssistantMessage,
					errorMessage: null,
				}),
				{
					headers: {
						"Content-Type": "application/json",
						"Access-Control-Allow-Origin": "*",
					},
				}
			);
		} catch (error: any) {
			return new Response(JSON.stringify({ success: false, errorMessage: "Invalid JSON body" }), {
				status: 400,
				headers: { "Content-Type": "application/json" },
			});
		}
	},
};
