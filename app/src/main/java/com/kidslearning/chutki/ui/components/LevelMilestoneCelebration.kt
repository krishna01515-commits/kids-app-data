package com.kidslearning.chutki.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kidslearning.chutki.theme.CandyOrange
import com.kidslearning.chutki.theme.CandyYellow
import com.kidslearning.chutki.theme.CorrectGreen
import kotlinx.coroutines.delay

@Composable
fun LevelMilestoneCelebration(
    isShowing: Boolean,
    level: Int,
    streak: Int,
    isEnglish: Boolean = false,
    onContinue: () -> Unit
) {
    if (!isShowing) return

    LaunchedEffect(isShowing) {
        delay(2600)
        onContinue()
    }

    Dialog(
        onDismissRequest = onContinue,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f)),
            contentAlignment = Alignment.Center
        ) {
            StarConfettiOverlay(trigger = true)

            AnimatedVisibility(
                visible = isShowing,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                ClayCard(
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .padding(16.dp),
                    cornerRadius = 28.dp,
                    elevation = 12.dp,
                    bgGradient = Brush.verticalGradient(
                        listOf(Color(0xFFFFF9E6), Color(0xFFFFECB3))
                    ),
                    borderHighlightColor = CandyOrange
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Badge Icon
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(listOf(CandyYellow, CandyOrange))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🏆", fontSize = 48.sp)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = if (isEnglish) "🌟 LEVEL UP! 🌟" else "🌟 लेवल अप! शाबाश! 🌟",
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp,
                            color = Color(0xFFE65100)
                        )

                        Text(
                            text = if (isEnglish) "You Reached Level $level!" else "आप लेवल $level पर पहुंच गए!",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = Color(0xFF3E2723)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Star row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "⭐", fontSize = 32.sp)
                            Text(text = "⭐", fontSize = 38.sp)
                            Text(text = "⭐", fontSize = 32.sp)
                        }

                        if (streak > 1) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFFF5722))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (isEnglish) "🔥 Combo Streak: x$streak!" else "🔥 कॉम्बो स्ट्रिक: x$streak!",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        ClayButton(
                            onClick = onContinue,
                            modifier = Modifier.fillMaxWidth(0.85f),
                            primaryColor = CorrectGreen,
                            shadowColor = Color(0xFF27AE60),
                            topHighlightColor = Color(0xFF58D68D)
                        ) {
                            Text(
                                text = if (isEnglish) "Keep Playing ▶" else "आगे खेलें ▶",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

