package com.techquantum.hushka

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class PromptBackupCodecTest {

    private fun sample(
        title: String = "عنوان",
        text: String = "متن [نمونه]",
        catalogId: String = "",
        locked: Boolean = false
    ) = PromptEntity(
        catalogId = catalogId,
        provider = "openai",
        modality = "text",
        title = title,
        category = "نوشتن",
        text = text,
        tags = "a,b",
        favorite = true,
        source = "prompts.chat:1",
        license = "CC0",
        collection = "col",
        role = "writer",
        pinned = true,
        lastUsedAt = 1234L,
        isLocked = locked
    )

    @Test
    fun roundTripKeepsAllFields() {
        val original = listOf(sample(catalogId = "cid-1", locked = true), sample(title = "دوم"))
        val parsed = PromptBackupCodec.parse(PromptBackupCodec.encode(original, 99L))
        assertEquals(0, parsed.invalid)
        // id در خروجی نیست و پس از ورود صفر است؛ بقیه‌ی فیلدها باید یکسان باشند.
        assertEquals(original.map { it.copy(id = 0) }, parsed.prompts)
    }

    @Test
    fun encodeWritesVersionedEnvelope() {
        val json = PromptBackupCodec.encode(listOf(sample()), 5L)
        assertTrue(json.contains("\"app\": \"hushka\""))
        assertTrue(json.contains("\"backupVersion\": 1"))
        assertTrue(json.contains("\"exportedAt\": 5"))
    }

    @Test
    fun readsLegacyPlainArray() {
        val legacy = """[{"id":7,"title":"قدیمی","category":"c","tags":"t","text":"x","collection":"k","role":"r"}]"""
        val parsed = PromptBackupCodec.parse(legacy)
        assertEquals(1, parsed.prompts.size)
        val p = parsed.prompts[0]
        assertEquals("قدیمی", p.title)
        assertEquals("k", p.collection)
        assertEquals("r", p.role)
        assertEquals("local", p.source)
        assertFalse(p.favorite)
        assertFalse(p.isLocked)
    }

    @Test
    fun countsInvalidItemsWithoutStopping() {
        val json = """{"app":"hushka","backupVersion":1,"prompts":[
            {"title":"ok","text":"a"},
            {"title":"  ","text":"blank title"},
            {"text":"no title"},
            "not an object",
            {"title":"ok2","text":"b"}
        ]}"""
        val parsed = PromptBackupCodec.parse(json)
        assertEquals(2, parsed.prompts.size)
        assertEquals(3, parsed.invalid)
    }

    @Test
    fun nullFieldsBecomeEmptyStrings() {
        val parsed = PromptBackupCodec.parse("""[{"title":"t","text":null,"tags":null}]""")
        assertEquals("", parsed.prompts[0].text)
        assertEquals("", parsed.prompts[0].tags)
    }

    @Test
    fun garbageThrowsBadFile() {
        for (bad in listOf("", "hello", "{not json", "{\"app\":\"hushka\"}", "[1,")) {
            try {
                PromptBackupCodec.parse(bad)
                fail("expected exception for: $bad")
            } catch (e: BackupParseException) {
                assertFalse(e.unsupportedVersion)
            }
        }
    }

    @Test
    fun newerVersionIsRejected() {
        try {
            PromptBackupCodec.parse("""{"app":"hushka","backupVersion":99,"prompts":[]}""")
            fail("expected unsupported version")
        } catch (e: BackupParseException) {
            assertTrue(e.unsupportedVersion)
        }
    }

    @Test
    fun dedupeByCatalogIdOrTitleAndText() {
        val existing = listOf(
            sample(title = "A", text = "1", catalogId = "cid-1"),
            sample(title = "B", text = "2")
        )
        val incoming = listOf(
            sample(title = "other", text = "x", catalogId = "cid-1"), // هم‌شناسه
            sample(title = " B ", text = " 2 "),                       // هم‌ترکیب با فاصله‌ی اضافه
            sample(title = "C", text = "3"),                           // جدید
            sample(title = "C", text = "3")                            // تکراری داخل فایل
        )
        val result = PromptBackupCodec.dedupe(incoming, existing)
        assertEquals(1, result.toAdd.size)
        assertEquals("C", result.toAdd[0].title)
        assertEquals(3, result.duplicates)
    }

    @Test
    fun sameTitleDifferentTextIsNotDuplicate() {
        val existing = listOf(sample(title = "A", text = "1"))
        val result = PromptBackupCodec.dedupe(listOf(sample(title = "A", text = "2")), existing)
        assertEquals(1, result.toAdd.size)
        assertEquals(0, result.duplicates)
    }
}
