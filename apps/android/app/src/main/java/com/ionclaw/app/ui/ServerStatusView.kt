package com.ionclaw.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ionclaw.app.ui.theme.BrandRunning
import com.ionclaw.app.ui.theme.BrandSuccess
import com.ionclaw.app.ui.theme.MutedText

// run-state indicator: a tinted halo around a solid dot
@Composable
fun ServerStatusView(isRunning: Boolean) {
    val dotColor = if (isRunning) BrandRunning else Color(0xFFBDBDBD)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(dotColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
        }

        Text(
            text = if (isRunning) "Running" else "Stopped",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = if (isRunning) BrandSuccess else MutedText
        )
    }
}
