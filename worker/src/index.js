const SYSTEM_PROMPT = `
You are NOVA, an independent personal AI assistant.
Your identity is NOVA, not Gemini or ChatGPT.
Speak naturally, clearly, and helpfully.
You can answer questions, explain concepts, reason,
and assist the user with tasks.
Always respond in the language used by the user.
`;

export default {
  async fetch(request, env) {
    const cors = {
      "Access-Control-Allow-Origin": "*",
      "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
      "Access-Control-Allow-Headers": "Content-Type, Authorization"
    };

    if (request.method === "OPTIONS") {
      return new Response(null, { headers: cors });
    }

    const json = (data, status = 200) =>
      Response.json(data, { status, headers: cors });

    if (request.method === "GET") {
      return json({
        name: "NOVA",
        status: "online",
        version: "1.0"
      });
    }

    if (request.method !== "POST") {
      return json({ error: "Method not allowed" }, 405);
    }

    try {
      const body = await request.json();
      const message = body.message;

      if (!message || typeof message !== "string") {
        return json({ error: "Message is required" }, 400);
      }

      const result = await env.AI.run(
        "@cf/zai-org/glm-4.7-flash",
        {
          messages: [
            { role: "system", content: SYSTEM_PROMPT },
            { role: "user", content: message }
          ]
        }
      );

      const reply =
        result?.response ??
        result?.choices?.[0]?.message?.content ??
        result?.choices?.[0]?.text ??
        result?.content;

      if (!reply) {
        return json({
          error: "AI returned an unexpected response format",
          modelResult: result
        }, 502);
      }

      return json({
        assistant: "NOVA",
        reply: reply
      });

    } catch (error) {
      return json({
        error: "NOVA AI request failed",
        detail: error.message
      }, 500);
    }
  }
};
