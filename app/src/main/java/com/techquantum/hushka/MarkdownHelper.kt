package com.techquantum.hushka

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp

object MarkdownHelper {
    fun render(text: String, textColor: Color, codeBgColor: Color): AnnotatedString {
        return buildAnnotatedString {
            val lines = text.split("\n")
            lines.forEachIndexed { index, line ->
                renderLine(line, textColor, codeBgColor)
                if (index < lines.size - 1) append("\n")
            }
        }
    }

    private fun androidx.compose.ui.text.AnnotatedString.Builder.renderLine(
        line: String, textColor: Color, codeBgColor: Color
    ) {
        val trimmed = line.trimStart()
        when {
            // بلاک کد: ```language
            trimmed.startsWith("```") -> {
                val codeContent = trimmed.removePrefix("```")
                withStyle(SpanStyle(
                    fontFamily = FontFamily.Monospace,
                    background = codeBgColor,
                    color = textColor,
                    fontSize = 12.sp
                )) {
                    append(codeContent.ifEmpty { " " })
                }
            }
            // هدر: #، ##، ###
            trimmed.startsWith("# ") -> {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textColor)) {
                    append(trimmed.removePrefix("# "))
                }
            }
            trimmed.startsWith("## ") -> {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textColor)) {
                    append(trimmed.removePrefix("## "))
                }
            }
            trimmed.startsWith("### ") -> {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textColor)) {
                    append(trimmed.removePrefix("### "))
                }
            }
            // لیست نقطه‌ای
            trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                append("• ")
                renderInline(trimmed.substring(2), textColor, codeBgColor)
            }
            else -> renderInline(line, textColor, codeBgColor)
        }
    }

    private fun androidx.compose.ui.text.AnnotatedString.Builder.renderInline(
        text: String, textColor: Color, codeBgColor: Color
    ) {
        var i = 0
        while (i < text.length) {
            // بولد: **text**
            if (i + 1 < text.length && text[i] == '*' && text[i + 1] == '*') {
                val end = text.indexOf("**", i + 2)
                if (end != -1) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = textColor)) {
                        append(text.substring(i + 2, end))
                    }
                    i = end + 2
                    continue
                }
            }
            // ایتالیک: *text*
            if (text[i] == '*') {
                val end = text.indexOf('*', i + 1)
                if (end != -1) {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = textColor)) {
                        append(text.substring(i + 1, end))
                    }
                    i = end + 1
                    continue
                }
            }
            // کد درون‌خطی: `code`
            if (text[i] == '`') {
                val end = text.indexOf('`', i + 1)
                if (end != -1) {
                    withStyle(SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        background = codeBgColor,
                        color = textColor,
                        fontSize = 12.sp
                    )) {
                        append(text.substring(i + 1, end))
                    }
                    i = end + 1
                    continue
                }
            }
            // متن ساده
            withStyle(SpanStyle(color = textColor)) { append(text[i]) }
            i++
        }
    }
}
