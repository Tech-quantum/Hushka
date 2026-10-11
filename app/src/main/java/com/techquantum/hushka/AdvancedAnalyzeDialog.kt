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
    val recommendationRes: Int
)

fun analyzePromptAdvanced(text: String): PromptAnalysis {
    val words = text.split(Regex("\\s+")).filter { it.isNotBlank() }
    val varCount = Regex("\\[([A-Z_]+)\\]").findAll(text).count() +
        Regex("\\{([^}]+)\\}").findAll(text).count()

    val clarity = when {
        words.size in 30..200 -> 19
        words.size in 15..29 -> 15
        words.size > 200 -> 14
        else -> 10
    }

    val role = when {
        text.contains("You are", true) && text.contains("act as", true) -> 20
        text.contains("You are", true) || text.contains("act as", true) -> 18
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
        !text.contains("You are", true) -> R.string.an_rec_role
        varCount == 0 -> R.string.an_rec_vars
        !text.contains("format", true) -> R.string.an_rec_format
        else -> R.string.an_rec_refusal
    }

    return PromptAnalysis(
        score = total, grade = grade, gradeLabelRes = gradeLabelRes,
        clarity = clarity, role = role, outputContract = outputContract,
        variables = variables, safety = safety,
        riskLevelRes = riskLevelRes, riskColor = riskColor, recommendationRes = recommendationRes
    )
}

@Composable
fun AdvancedAnalyzeDialog(
    prompt: PromptEntity,
    onDismiss: () -> Unit,
    onReplace: () -> Unit,
    onSaveAsNew: () -> Unit,
    onTestInSimulator: () -> Unit
) {
    val context = LocalContext.current
    val analysis = remember(prompt.text) { analyzePromptAdvanced(prompt.text) }
    val bgColor = Color(0xFF0F0F1E)
    val cardColor = Color(0xFF1A1A2E)
    val cardLight = Color(0xFF252538)
    val purplePrimary = Color(0xFF7C7CFF)
    val textColor = Color(0xFFEAEAEA)
    val textGray = Color(0xFF9E9EAE)

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
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Text("✕", fontSize = 20.sp, color = textGray)
                    }
                    Spacer(Modifier.weight(1f))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            stringResource(R.string.an_title),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(
                            stringResource(R.string.an_subtitle),
                            fontSize = 10.sp,
                            color = textGray
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFFB74D).copy(alpha = 0.2f),
                        modifier = Modifier.size(36.dp)
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
                        .padding(horizontal = 16.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = purplePrimary.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("⚡", fontSize = 12.sp)
                                Spacer(Modifier.width(4.dp))
                                Text("AI Prompt Studio", fontSize = 11.sp, color = purplePrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // ===== Main Score Card =====
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = cardColor,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
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
                                        color = Color(0xFF4CAF50).copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            stringResource(analysis.gradeLabelRes),
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            fontSize = 11.sp,
                                            color = Color(0xFF4CAF50),
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
                                                    Color(0xFF4CAF50).copy(alpha = 0.35f),
                                                    Color(0xFF4CAF50).copy(alpha = 0.08f)
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
                                            color = Color(0xFF4CAF50)
                                        )
                                        Text(stringResource(R.string.an_out_of), fontSize = 9.sp, color = textGray)
                                    }
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            // Risk badge
                            Surface(
                                shape = RoundedCornerShape(12.dp),
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

                    Spacer(Modifier.height(16.dp))

                    // ===== Detailed Scores =====
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = cardColor,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            DetailBar(stringResource(R.string.an_clarity), "(Clarity)", analysis.clarity, 20, Color(0xFF7C7CFF), textColor, textGray)
                            DetailBar(stringResource(R.string.an_role), "(Role)", analysis.role, 20, Color(0xFFB87CFF), textColor, textGray)
                            DetailBar(stringResource(R.string.an_output), "(Output Contract)", analysis.outputContract, 20, Color(0xFF4CAF50), textColor, textGray)
                            DetailBar(stringResource(R.string.an_vars), "(Variables)", analysis.variables, 20, Color(0xFFFFB74D), textColor, textGray)
                            DetailBar(stringResource(R.string.an_safety), "(Safety & Guardrails)", analysis.safety, 20, Color(0xFFE57373), textColor, textGray)
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // ===== Recommendation =====
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = purplePrimary.copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("✨", fontSize = 18.sp)
                            Spacer(Modifier.width(10.dp))
                            Text(
                                stringResource(analysis.recommendationRes),
                                fontSize = 12.sp,
                                color = textColor,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                }

                // ===== Bottom Action Buttons =====
                Surface(
                    color = cardColor,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                onReplace()
                                Toast.makeText(context, context.getString(R.string.an_toast_updated), Toast.LENGTH_SHORT).show()
                                onDismiss()
                            },
                            modifier = Modifier.weight(1.2f).height(56.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50),
                                contentColor = Color.White
                            )
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(stringResource(R.string.an_replace_a), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(stringResource(R.string.an_replace_b), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = {
                                onSaveAsNew()
                                Toast.makeText(context, context.getString(R.string.an_toast_saved), Toast.LENGTH_SHORT).show()
                                onDismiss()
                            },
                            modifier = Modifier.weight(1.1f).height(56.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFB74D).copy(alpha = 0.25f),
                                contentColor = Color(0xFFFFB74D)
                            )
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(stringResource(R.string.an_save_a), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(stringResource(R.string.an_save_b), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = onTestInSimulator,
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF64B5F6).copy(alpha = 0.25f),
                                contentColor = Color(0xFF64B5F6)
                            )
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(stringResource(R.string.an_test_a), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(stringResource(R.string.an_test_b), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        IconButton(
                            onClick = {
                                Toast.makeText(context, context.getString(R.string.an_toast_history), Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Text("🔄", fontSize = 16.sp, color = textGray)
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
