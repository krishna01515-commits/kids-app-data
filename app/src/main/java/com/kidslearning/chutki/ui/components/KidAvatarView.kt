package com.kidslearning.chutki.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kidslearning.chutki.R

/**
 * Renders the circular 3D Kid Avatar (Girl or Boy) across the entire app
 * replacing generic emojis with the uploaded 3D character photos.
 */
@Composable
fun KidAvatarView(
    avatarKey: String,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier,
    borderColor: Color = Color.White,
    borderWidth: Dp = 2.dp,
    showBorder: Boolean = true
) {
    val isBoy = avatarKey.contains("boy", ignoreCase = true) ||
            avatarKey == "👦" ||
            avatarKey.contains("chintu", ignoreCase = true) ||
            avatarKey.contains("वीर", ignoreCase = true) ||
            avatarKey.contains("आशू", ignoreCase = true) ||
            avatarKey.contains("रोहन", ignoreCase = true) ||
            avatarKey.contains("अमन", ignoreCase = true)

    val drawableRes = if (isBoy) R.drawable.avatar_boy else R.drawable.avatar_girl

    Box(
        modifier = modifier
            .size(size)
            .then(
                if (showBorder) {
                    Modifier
                        .clip(CircleShape)
                        .border(borderWidth, borderColor, CircleShape)
                } else {
                    Modifier.clip(CircleShape)
                }
            )
            .background(
                Brush.radialGradient(
                    if (isBoy) listOf(Color(0xFFE1F5FE), Color(0xFF81D4FA))
                    else listOf(Color(0xFFFCE4EC), Color(0xFFF48FB1))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = drawableRes),
            contentDescription = if (isBoy) "3D Boy Avatar" else "3D Girl Avatar",
            modifier = Modifier.size(size),
            contentScale = ContentScale.Crop
        )
    }
}

