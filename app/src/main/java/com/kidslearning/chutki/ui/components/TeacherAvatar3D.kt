package com.kidslearning.chutki.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidslearning.chutki.R

enum class TeacherPose {
    WELCOME_WAVE,
    POINTING_AT_CARD,
    CHEERING_STARS,
    THINKING
}

@Composable
fun TeacherAvatar3D(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    pose: TeacherPose = TeacherPose.POINTING_AT_CARD,
    isSpeaking: Boolean = true,
    flipHorizontal: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "tutor_animation")

    // Gentle breathing/bobbing animation
    val bobbingOffset by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tutor_bob"
    )

    // Speaking gentle pulse
    val speakingScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = if (isSpeaking) 1.03f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(280, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tutor_speak"
    )

    // Star sparkle animation at pen tip
    val sparkleScale by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparkle_scale"
    )

    // Aspect ratio of the original Priya Didi teacher image is 377:661
    val avatarWidth = size * 0.57f
    val avatarHeight = size

    val shouldFlip = flipHorizontal || (pose == TeacherPose.POINTING_AT_CARD && !flipHorizontal)

    Box(
        modifier = modifier
            .width(avatarWidth)
            .height(avatarHeight)
            .offset(y = bobbingOffset.dp)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Soft bottom contact shadow
        Box(
            modifier = Modifier
                .width(avatarWidth * 0.75f)
                .height(10.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0x33000000), Color.Transparent)
                    )
                )
        )

        // 3D Teacher Model Image (Priya Didi)
        Image(
            painter = painterResource(
                id = if (shouldFlip) R.drawable.teacher_priya_flipped else R.drawable.teacher_priya
            ),
            contentDescription = "3D Teacher Priya Didi",
            modifier = Modifier
                .fillMaxSize()
                .scale(speakingScale),
            contentScale = ContentScale.Fit
        )

        // Glowing sparkle star near the pointer/pen
        if (pose == TeacherPose.POINTING_AT_CARD || isSpeaking || pose == TeacherPose.CHEERING_STARS) {
            Box(
                modifier = Modifier
                    .align(if (shouldFlip) Alignment.TopEnd else Alignment.TopStart)
                    .offset(
                        x = if (shouldFlip) (-2).dp else 2.dp,
                        y = (avatarHeight * 0.32f)
                    )
                    .scale(sparkleScale)
            ) {
                Text(text = "✨", fontSize = (size.value * 0.16f).sp)
            }
        }
    }
}
