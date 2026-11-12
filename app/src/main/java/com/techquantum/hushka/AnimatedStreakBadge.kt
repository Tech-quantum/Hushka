package com.techquantum.hushka

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AnimatedStreakBadge(streak: Int) {
    if (streak <= 0) return

    Surface(
        shape = RoundedCornerShape(50),
        color = Color(0xFF4A3B2C), // رنگ قهوه‌ای/نارنجی ملایم مشابه عکس
        modifier = Modifier.padding(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🔥",
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = streak.toString(),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFFFFA726) // رنگ نارنجی برای عدد
            )
        }
    }
}
