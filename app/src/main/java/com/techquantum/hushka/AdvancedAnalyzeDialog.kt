package com.techquantum.hushka

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

data class PromptAnalysis(
    val score: Int,
    val grade: String,
    val gradeLabelRes: Int,
    val clarity: Int,
    val role: Int,
    val outputContract: Int,
    val variables: Int,
    val safety: Int,
    val riskLevelRes: Int,
    val riskColor: Color,
    val recommendationRes: Int,
    val hasRole: Boolean
)

// الگوها یک بار ساخته می‌شوند (ساختن Regex در هر بار آنالیز هزینه‌ی زیادی دارد)
private val BRACKET_VAR_REGEX = Regex("\\[([A-Z_]+)\\]")
private val CURLY_VAR_REGEX = Regex("\\{([^}]+)\\}")

private fun countWords(text: String): Int {
    var count = 0
    var inWord = false
    for (c in text) {
        if (c.isWhitespace()) {
            inWord = false
        } else if (!inWord) {
            inWord = true
            count++
        }
    }
    return count
}

fun analyzePromptAdvanced(text: String): PromptAnalysis {
    val wordCount = countWords(text)
    val varCount = BRACKET_VAR_REGEX.findAll(text).count() +
        CURLY_VAR_REGEX.findAll(text).count()

    val clarity = when {
        wordCount in 30..200 -> 19
        wordCount in 15..29 -> 15
        wordCount > 200 -> 14
        else -> 10
    }

    val hasRole = RoleSuggester.hasRoleDefinition(text)
    val role = when {
        hasRole && text.contains("You are", true) && text.contains("act as", true) -> 20
        hasRole -> 18
        else -> 10
    }

    var outputContract = 10
    if (text.contains("format", true)) outputContract += 4
    if (text.contains("structure", true)) outputContract += 3
    if (text.contains("step", true)) outputContract += 3
    outputContract = outputContract.coerceAtMost(20)

    val variables = when {
        varCount >= 5 -> 19
        varCount >= 3 -> 17
        varCount >= 1 -> 12
        else -> 8
    }

    var safety = 12
    if (text.contains("refusal", true) || text.contains("guardrail", true)) safety += 4
    if (text.contains("safety", true)) safety += 3
    if (text.contains("must not", true) || text.contains("do not", true)) safety += 3
    safety = safety.coerceAtMost(20)

    val total = clarity + role + outputContract + variables + safety

    val grade = when {
        total >= 90 -> "A+"
        total >= 85 -> "A"
        total >= 75 -> "B"
        total >= 60 -> "C"
        else -> "D"
    }
    val gradeLabelRes = when {
        total >= 85 -> R.string.an_grade_vstrong
        total >= 75 -> R.string.an_grade_strong
        total >= 60 -> R.string.an_grade_medium
        else -> R.string.an_grade_weak
    }
    val riskLevelRes = when {
        total >= 80 -> R.string.an_risk_low
        total >= 60 -> R.string.an_risk_mid
        else -> R.string.an_risk_high
    }
    val riskColor = when {
        total >= 80 -> Color(0xFF4CAF50)
        total >= 60 -> Color(0xFFFFB74D)
        else -> Color(0xFFE57373)
    }
    val recommendationRes = when {
        !hasRole -> R.string.an_rec_role
        varCount == 0 -> R.string.an_rec_vars
        !text.contains("format", true) -> R.string.an_rec_format
        else -> R.string.an_rec_refusal
    }

    return PromptAnalysis(
        score = total, grade = grade, gradeLabelRes = gradeLabelRes,
        clarity = clarity, role = role, outputContract = outputContract,
        variables = variables, safety = safety,
        riskLevelRes = riskLevelRes, riskColor = riskColor, recommendationRes = recommendationRes,
        hasRole = hasRole
    )
}

@Composable
fun AdvancedAnalyzeDialog(
    prompt: PromptEntity,
    onDismiss: () -> Unit,
    onReplace: () -> Unit,
    onSaveAsNew: () -> Unit,
    onTestInSimulator: () -> Unit,
    onApplyRole: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val analysis = remember(prompt.text) { analyzePromptAdvanced(prompt.text) }
    // برای همه‌ی پرامپت‌هایی که نقش ندارند یک نقش پیشنهاد می‌شود
    val suggestion = remember(prompt.id, prompt.text) {
        if (analysis.hasRole) null else RoleSuggester.suggest(prompt)
    }

    // رنگ‌ها از تم برنامه گرفته می‌شود تا با بقیه‌ی صفحه‌ها یکسان باشد
    val bgColor = MaterialTheme.colorScheme.background
    val cardColor = MaterialTheme.colorScheme.surface
    val cardLight = MaterialTheme.colorScheme.surfaceVariant
    val purplePrimary = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onSurface
    val textGray = MaterialTheme.colorScheme.onSurfaceVariant
    val goodColor = Color(0xFF4CAF50)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            color = bgColor
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // ===== Top Bar =====
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Text("✕", fontSize = 20.sp, color = textGray)
                    }
                    Spacer(Modifier.weight(1f))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            stringResource(R.string.an_title),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(
                            stringResource(R.string.an_subtitle),
                            fontSize = 11.sp,
                            color = textGray
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Surface(
                        shape = CircleShape,
                        color = purplePrimary.copy(alpha = 0.15f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("✨", fontSize = 16.sp)
                        }
                    }
                }

                // ===== Scrollable Content =====
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // ===== Main Score Card =====
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = cardColor,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(stringResource(R.string.an_grade_label), fontSize = 13.sp, color = textGray)
                                        Spacer(Modifier.width(6.dp))
                                        Text(analysis.grade, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = textColor)
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = goodColor.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            stringResource(analysis.gradeLabelRes),
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            fontSize = 11.sp,
                                            color = goodColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(Modifier.height(10.dp))
                                    Text(
                                        stringResource(R.string.an_standards),
                                        fontSize = 10.sp,
                                        color = textGray,
                                        lineHeight = 14.sp
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Box(
                                    modifier = Modifier
                                        .size(84.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.radialGradient(
                                                listOf(
                                                    goodColor.copy(alpha = 0.30f),
                                                    goodColor.copy(alpha = 0.06f)
                                                )
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            "${analysis.score}",
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = goodColor
                                        )
                                        Text(stringResource(R.string.an_out_of), fontSize = 9.sp, color = textGray)
                                    }
                                }
                            }

                            Spacer(Modifier.height(14.dp))

                            // Risk badge
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = cardLight,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("⚠️", fontSize = 14.sp)
                                    Spacer(Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(stringResource(R.string.an_risk_label), fontSize = 10.sp, color = textGray)
                                        Text(
                                            stringResource(analysis.riskLevelRes),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = analysis.riskColor
                                        )
                                    }
                                    Surface(
                                        shape = CircleShape,
                                        color = analysis.riskColor.copy(alpha = 0.2f),
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("🛡", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ===== Detailed Scores =====
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = cardColor,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            DetailBar(stringResource(R.string.an_clarity), "(Clarity)", analysis.clarity, 20, Color(0xFF7C7CFF), textColor, textGray)
                            DetailBar(stringResource(R.string.an_role), "(Role)", analysis.role, 20, Color(0xFFB87CFF), textColor, textGray)
                            DetailBar(stringResource(R.string.an_output), "(Output Contract)", analysis.outputContract, 20, Color(0xFF4CAF50), textColor, textGray)
                            DetailBar(stringResource(R.string.an_vars), "(Variables)", analysis.variables, 20, Color(0xFFFFB74D), textColor, textGray)
                            DetailBar(stringResource(R.string.an_safety), "(Safety & Guardrails)", analysis.safety, 20, Color(0xFFE57373), textColor, textGray)
                        }
                    }

                    // ===== Recommendation =====
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = purplePrimary.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.Top) {
                                Text("✨", fontSize = 18.sp)
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    if (suggestion != null) {
                                        val roleName = suggestion.template?.displayName()
                                            ?: if (suggestion.topic.isNotBlank()) {
                                                stringResource(R.string.role_auto_name, suggestion.topic)
                                            } else {
                                                stringResource(R.string.role_auto_generic)
                                            }
                                        stringResource(R.string.an_rec_role_suggest, roleName)
                                    } else {
                                        stringResource(analysis.recommendationRes)
                                    },
                                    fontSize = 12.sp,
                                    color = textColor,
                                    lineHeight = 18.sp
                                )
                            }

                            if (suggestion != null) {
                                Spacer(Modifier.height(12.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = cardColor,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        "${suggestion.emoji}  ${suggestion.prefix}",
                                        modifier = Modifier.padding(12.dp),
                                        fontSize = 11.sp,
                                        color = textGray,
                                        lineHeight = 16.sp
                                    )
                                }
                                Spacer(Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        onApplyRole(RoleSuggester.applyRole(prompt.text, suggestion.prefix))
                                        Toast.makeText(context, context.getString(R.string.an_role_applied), Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text(stringResource(R.string.an_apply_role), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(4.dp))
                }

                // ===== Bottom Action Buttons =====
                Surface(
                    color = cardColor,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                onReplace()
                                Toast.makeText(context, context.getString(R.string.an_toast_updated), Toast.LENGTH_SHORT).show()
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f).heightIn(min = 56.dp),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                        ) {
                            Text(
                                stringResource(R.string.an_replace_a) + " " + stringResource(R.string.an_replace_b),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                lineHeight = 15.sp,
                                maxLines = 3
                            )
                        }

                        FilledTonalButton(
                            onClick = {
                                onSaveAsNew()
                                Toast.makeText(context, context.getString(R.string.an_toast_saved), Toast.LENGTH_SHORT).show()
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f).heightIn(min = 56.dp),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                        ) {
                            Text(
                                stringResource(R.string.an_save_a) + " " + stringResource(R.string.an_save_b),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                lineHeight = 15.sp,
                                maxLines = 3
                            )
                        }

                        OutlinedButton(
                            onClick = onTestInSimulator,
                            modifier = Modifier.weight(1f).heightIn(min = 56.dp),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                        ) {
                            Text(
                                stringResource(R.string.an_test_a) + " " + stringResource(R.string.an_test_b),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                lineHeight = 15.sp,
                                maxLines = 3
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailBar(
    label: String,
    sublabel: String,
    score: Int,
    maxScore: Int,
    barColor: Color,
    textColor: Color,
    textGray: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "$score/$maxScore",
                fontSize = 12.sp,
                color = textColor,
                fontWeight = FontWeight.Medium
            )
            Row {
                Text(label, fontSize = 12.sp, color = textColor)
                Spacer(Modifier.width(4.dp))
                Text(sublabel, fontSize = 10.sp, color = textGray)
            }
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { score.toFloat() / maxScore },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = barColor,
            trackColor = barColor.copy(alpha = 0.15f)
        )
    }
}
