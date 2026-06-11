package com.cvanalyzer.service.ai;

/**
 * Best-effort extraction and repair of a JSON object embedded in free-form LLM
 * output. Handles the common ways OpenAI / LM Studio wrap or slightly mangle
 * JSON: markdown code fences, leading prose, trailing commas, smart quotes,
 * and truncated objects with unbalanced braces.
 */
public final class JsonExtractor {

    private JsonExtractor() {}

    /** Pull the most plausible JSON object out of arbitrary text. */
    public static String extract(String raw) {
        if (raw == null) return null;
        String text = raw.trim();

        // Strip markdown fences ```json ... ```
        if (text.startsWith("```")) {
            int firstNewline = text.indexOf('\n');
            if (firstNewline > 0) {
                text = text.substring(firstNewline + 1);
            }
            int fenceEnd = text.lastIndexOf("```");
            if (fenceEnd >= 0) {
                text = text.substring(0, fenceEnd);
            }
            text = text.trim();
        }

        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        // Object opened but never closed (truncated output) — keep from first brace.
        if (start >= 0) {
            return text.substring(start);
        }
        return text;
    }

    /** Apply lenient fixes for the malformations models most often emit. */
    public static String repair(String json) {
        if (json == null) return null;
        String s = json.trim();

        // Normalise smart quotes to straight quotes.
        s = s.replace('“', '"').replace('”', '"')
             .replace('‘', '\'').replace('’', '\'');

        // Remove trailing commas before } or ]  ->  {"a":1,}  becomes {"a":1}
        s = s.replaceAll(",\\s*([}\\]])", "$1");

        // Balance braces/brackets if the model truncated the object.
        s = balance(s, '{', '}');
        s = balance(s, '[', ']');
        return s;
    }

    private static String balance(String s, char open, char close) {
        int opens = count(s, open);
        int closes = count(s, close);
        StringBuilder sb = new StringBuilder(s);
        for (int i = 0; i < opens - closes; i++) {
            sb.append(close);
        }
        return sb.toString();
    }

    private static int count(String s, char c) {
        int n = 0;
        boolean inString = false;
        char prev = 0;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == '"' && prev != '\\') inString = !inString;
            if (!inString && ch == c) n++;
            prev = ch;
        }
        return n;
    }
}
