package com.kidslearning.chutki.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ClayCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 26.dp,
    elevation: Dp = 8.dp,
    bgGradient: Brush = Brush.verticalGradient(
        colors = listOf(Color.White, Color(0xFFFFF9E6))
    ),
    borderHighlightColor: Color = Color.White.copy(alpha = 0.8f),
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = Color(0x33000000),
                spotColor = Color(0x22000000)
            )
            .clip(shape)
            .background(bgGradient)
            .border(
                width = 2.5.dp,
                color = borderHighlightColor,
                shape = shape
            )
    ) {
        content()
    }
}

