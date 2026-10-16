import { onRequest } from "firebase-functions/v2/https";
import { defineSecret } from "firebase-functions/params";
import { timingSafeEqual } from "crypto";

const GEMINI_API_KEY = defineSecret("GEMINI_API_KEY");
const APP_TOKEN = defineSecret("APP_TOKEN");

const ALLOWED_MODELS = new Set(["gemini-2.5-flash", "gemini-2.5-pro", "gemini-2.5-flash-lite"]);
const MAX_MESSAGES = 40;
const MAX_TEXT = 4000;
const LIMIT_PER_MIN = 20;
const hits = new Map<string, number[]>(); // in-memory, per instance

function authorized(header: string | undefined, token: string): boolean {
  const got = Buffer.from((header ?? "").replace(/^Bearer /, ""));
  const want = Buffer.from(token);
  return got.length === want.length && timingSafeEqual(got, want);
}

function limited(key: string): boolean {
  const now = Date.now();
  const arr = (hits.get(key) ?? []).filter((t) => now - t < 60_000);
  arr.push(now);
  hits.set(key, arr);
  return arr.length > LIMIT_PER_MIN;
}

export const api = onRequest(
  { secrets: [GEMINI_API_KEY, APP_TOKEN], timeoutSeconds: 60, maxInstances: 3 },
  async (req, res) => {
    if (!authorized(req.header("authorization"), APP_TOKEN.value())) {
      res.status(401).json({ error: "unauthorized" });
      return;
    }
    if (limited(req.ip ?? "unknown")) {
      res.status(429).json({ error: "rate_limited" });
      return;
    }
    if (req.path === "/health") {
      res.json({ ok: true });
      return;
    }
    if (req.path !== "/chat" || req.method !== "POST") {
      res.status(404).json({ error: "not_found" });
      return;
    }

    const { model, system, messages } = req.body ?? {};
    if (!ALLOWED_MODELS.has(model) || !Array.isArray(messages) || messages.length === 0 || messages.length > MAX_MESSAGES) {
      res.status(400).json({ error: "bad_request" });
      return;
    }
    const contents = [];
    for (const m of messages) {
      if (typeof m?.text !== "string" || m.text.length > MAX_TEXT || !["user", "assistant"].includes(m.role)) {
        res.status(400).json({ error: "bad_request" });
        return;
      }
      contents.push({ role: m.role === "assistant" ? "model" : "user", parts: [{ text: m.text }] });
    }

    try {
      const r = await fetch(`https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent`, {
        method: "POST",
        headers: { "Content-Type": "application/json", "x-goog-api-key": GEMINI_API_KEY.value() },
        body: JSON.stringify({
          systemInstruction: { parts: [{ text: String(system ?? "").slice(0, 4000) }] },
          contents,
        }),
      });
      if (!r.ok) {
        res.status(r.status === 429 ? 429 : 502).json({ error: "upstream_error" });
        return;
      }
      const data: any = await r.json();
      const text = data?.candidates?.[0]?.content?.parts?.map((p: any) => p.text ?? "").join("") ?? "";
      res.json({ text });
    } catch {
      res.status(502).json({ error: "upstream_error" });
    }
  }
);
