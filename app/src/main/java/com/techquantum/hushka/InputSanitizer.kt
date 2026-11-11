package com.techquantum.hushka

object InputSanitizer {
    private val SPECIAL_TOKENS = listOf(
        "<|im_start|>", "<|im_end|>", "<|system|>", "<|user|>", "<|assistant|>",
        "<|endoftext|>", "<|end|>", "<|start|>",
        "[INST]", "[/INST]", "<<SYS>>", "<</SYS>>",
        "<s>", "</s>", "<unk>", "<pad>"
    )

    private val CONTROL_CHARS = Regex("[\u0000-\u0008\u000B\u000C\u000E-\u001F\u007F]")

    fun sanitizeForAi(text: String): String {
        if (text.isBlank()) return text
        var r = CONTROL_CHARS.replace(text, "")
        SPECIAL_TOKENS.forEach { t -> r = r.replace(t, "", ignoreCase = false) }
        return r.trim()
    }

    fun sanitizeWebResult(text: String, maxLength: Int = 4000): String {
        if (text.isBlank()) return text
        return sanitizeForAi(text).take(maxLength).trim()
    }

    fun sanitizeForStorage(text: String): String {
        if (text.isBlank()) return text
        var r = CONTROL_CHARS.replace(text, "")
        r = Regex("<script[^>]*>.*?</script>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)).replace(r, "")
        r = Regex("(?i)javascript:").replace(r, "")
        r = Regex("(?i)data:(text/html|application/javascript)").replace(r, "")
        return r.trim()
    }
}
