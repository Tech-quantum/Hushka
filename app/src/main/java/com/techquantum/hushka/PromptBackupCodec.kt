package com.techquantum.hushka

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/** خطای قالب فایل پشتیبان (JSON خراب یا نسخه‌ی ناشناخته). */
class BackupParseException(val unsupportedVersion: Boolean = false, cause: Throwable? = null) :
    Exception(if (unsupportedVersion) "Unsupported backup version" else "Invalid backup file", cause)

/** نتیجه‌ی نهایی ورود پشتیبان، برای نمایش به کاربر. */
sealed class BackupImportOutcome {
    data class Done(val added: Int, val duplicates: Int, val invalid: Int) : BackupImportOutcome()
    object BadFile : BackupImportOutcome()
    object UnsupportedVersion : BackupImportOutcome()
    object ReadError : BackupImportOutcome()
}

data class ParsedBackup(val prompts: List<PromptEntity>, val invalid: Int)

data class DedupeResult(val toAdd: List<PromptEntity>, val duplicates: Int)

/**
 * منطق خالص (بدون Context) خروجی/ورودی پشتیبان.
 * قالب جدید: {"app":"hushka","backupVersion":1,"exportedAt":...,"prompts":[...]}
 * قالب قدیمی: آرایه‌ی ساده‌ی JSON از پرامپت‌ها (همچنان خوانده می‌شود).
 * کلیدهای API هرگز در پشتیبان نوشته نمی‌شوند (PromptEntity فیلد کلید ندارد).
 */
object PromptBackupCodec {
    const val APP_NAME = "hushka"
    const val BACKUP_VERSION = 1

    fun encode(prompts: List<PromptEntity>, exportedAt: Long): String {
        val array = JSONArray()
        prompts.forEach { p ->
            array.put(
                JSONObject().apply {
                    put("title", p.title)
                    put("category", p.category)
                    put("text", p.text)
                    put("tags", p.tags)
                    put("collection", p.collection)
                    put("role", p.role)
                    put("favorite", p.favorite)
                    put("pinned", p.pinned)
                    put("lastUsedAt", p.lastUsedAt)
                    put("isLocked", p.isLocked)
                    put("catalogId", p.catalogId)
                    put("provider", p.provider)
                    put("modality", p.modality)
                    put("source", p.source)
                    put("license", p.license)
                }
            )
        }
        val root = JSONObject().apply {
            put("app", APP_NAME)
            put("backupVersion", BACKUP_VERSION)
            put("exportedAt", exportedAt)
            put("prompts", array)
        }
        return root.toString(2)
    }

    fun parse(content: String): ParsedBackup {
        val trimmed = content.trim().removePrefix("\uFEFF")
        val array: JSONArray = try {
            when {
                trimmed.startsWith("[") -> JSONArray(trimmed)
                trimmed.startsWith("{") -> {
                    val root = JSONObject(trimmed)
                    if (root.optInt("backupVersion", BACKUP_VERSION) > BACKUP_VERSION) {
                        throw BackupParseException(unsupportedVersion = true)
                    }
                    root.optJSONArray("prompts") ?: throw BackupParseException()
                }
                else -> throw BackupParseException()
            }
        } catch (e: JSONException) {
            throw BackupParseException(cause = e)
        }

        val result = ArrayList<PromptEntity>()
        var invalid = 0
        for (i in 0 until array.length()) {
            val obj = array.optJSONObject(i)
            if (obj == null) {
                invalid++
                continue
            }
            val title = str(obj, "title").trim()
            if (title.isEmpty()) {
                invalid++
                continue
            }
            val source = str(obj, "source")
            result.add(
                PromptEntity(
                    catalogId = str(obj, "catalogId"),
                    provider = str(obj, "provider"),
                    modality = str(obj, "modality"),
                    title = title,
                    category = str(obj, "category"),
                    text = str(obj, "text"),
                    tags = str(obj, "tags"),
                    favorite = obj.optBoolean("favorite", false),
                    source = if (source.isEmpty()) "local" else source,
                    license = str(obj, "license"),
                    collection = str(obj, "collection"),
                    role = str(obj, "role"),
                    pinned = obj.optBoolean("pinned", false),
                    lastUsedAt = obj.optLong("lastUsedAt", 0L),
                    isLocked = obj.optBoolean("isLocked", false)
                )
            )
        }
        return ParsedBackup(result, invalid)
    }

    /**
     * موارد تکراری را حذف می‌کند: هم‌شناسه‌ی کاتالوگ (غیرخالی) یا هم‌ترکیب (عنوان + متن).
     * موارد تکراری داخل خود فایل هم یک‌بار شمرده می‌شوند.
     */
    fun dedupe(incoming: List<PromptEntity>, existing: List<PromptEntity>): DedupeResult {
        val catalogIds = HashSet<String>()
        val pairs = HashSet<Pair<String, String>>()
        existing.forEach {
            if (it.catalogId.isNotBlank()) catalogIds.add(it.catalogId)
            pairs.add(it.title.trim() to it.text.trim())
        }
        val toAdd = ArrayList<PromptEntity>()
        var duplicates = 0
        for (p in incoming) {
            val pair = p.title.trim() to p.text.trim()
            val isDup = (p.catalogId.isNotBlank() && p.catalogId in catalogIds) || pair in pairs
            if (isDup) {
                duplicates++
            } else {
                toAdd.add(p)
                if (p.catalogId.isNotBlank()) catalogIds.add(p.catalogId)
                pairs.add(pair)
            }
        }
        return DedupeResult(toAdd, duplicates)
    }

    // optString در org.json برای null مقدار "null" برمی‌گرداند؛ این‌جا رشته‌ی خالی می‌خواهیم.
    private fun str(obj: JSONObject, key: String): String =
        if (obj.isNull(key)) "" else obj.optString(key, "")
}
