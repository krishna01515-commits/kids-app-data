package com.kidslearning.chutki.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidslearning.chutki.audio.HindiTtsManager
import com.kidslearning.chutki.audio.SoundEffectHelper
import com.kidslearning.chutki.data.LearningContentRepository
import com.kidslearning.chutki.data.UserPreferencesManager
import com.kidslearning.chutki.model.SubjectType
import com.kidslearning.chutki.theme.CandyOrange
import com.kidslearning.chutki.theme.CandyYellow
import com.kidslearning.chutki.theme.CorrectGreen
import com.kidslearning.chutki.ui.components.ClayButton
import com.kidslearning.chutki.ui.components.ClayCard
import com.kidslearning.chutki.ui.components.StarConfettiOverlay
import com.kidslearning.chutki.ui.components.TeacherAvatar3D
import com.kidslearning.chutki.ui.components.TeacherPose

@Composable
fun Level3WordBuilderScreen(
    subject: SubjectType,
    isDarkMode: Boolean,
    ttsManager: HindiTtsManager?,
    userPrefs: UserPreferencesManager? = null,
    onBack: () -> Unit,
    onNavigateToQuiz: () -> Unit,
    onAddReward: (xp: Int, stars: Int) -> Unit = { _, _ -> },
    onWordBuilt: (wordId: String) -> Unit = {}
) {
    val blends = remember(subject.id) {
        LearningContentRepository.getWordBlends(subject.id)
    }

    val savedIndex = remember(subject.id) {
        userPrefs?.getLastBlendIndex(subject.id) ?: 0
    }

    var blendIndex by remember(subject.id) {
        mutableIntStateOf(savedIndex.coerceIn(0, (blends.size - 1).coerceAtLeast(0)))
    }
    val currentBlend = blends.getOrNull(blendIndex) ?: blends.first()

    var isMerged by remember { mutableStateOf(false) }
    var showConfetti by remember { mutableStateOf(false) }

    val accentColor = Color(subject.themeColorHex)

    // Snap / Scale animation
    val mergeScale by animateFloatAsState(
        targetValue = if (isMerged) 1.08f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "merge_scale"
    )

    // Reset when changing blend & persist progress
    LaunchedEffect(blendIndex) {
        isMerged = false
        showConfetti = false
        userPrefs?.setLastBlendIndex(subject.id, blendIndex, currentBlend.resultWord)
        val intro = when (subject) {
            SubjectType.HINDI -> "चलो दो अक्षर जोड़कर नया शब्द बनाते हैं! '${currentBlend.char1}' और '${currentBlend.char2}' को मिलाओ!"
            SubjectType.ENGLISH -> "Let's blend two letters! '${currentBlend.char1}' plus '${currentBlend.char2}'!"
            SubjectType.MATH -> "चलो दो संख्याओं को जोड़ते हैं! '${currentBlend.char1}' और '${currentBlend.char2}' कितने होते हैं?"
        }
        ttsManager?.speak(intro)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (isDarkMode) Brush.verticalGradient(listOf(Color(0xFF13142B), Color(0xFF1E2142)))
                else Brush.verticalGradient(listOf(Color(0xFFFFFDF5), Color(0xFFFFF3E0)))
            )
            .padding(18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (isDarkMode) Color(0xFF262A56) else Color(0xFFFFECB3))
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "⬅️", fontSize = 18.sp)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "लेवल 3: अक्षर जोड़ना सीखो 🧩",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = if (isDarkMode) Color.White else Color(0xFF263238)
                    )
                    Text(
                        text = "शब्द निर्माण (${blendIndex + 1}/${blends.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CandyOrange
                    )
                }

                // Audio Replay Button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                        .clickable {
                            if (isMerged) {
                                ttsManager?.speak(currentBlend.audioExplanationHindi)
                            } else {
                                ttsManager?.speak("${currentBlend.char1} और ${currentBlend.char2}")
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🔊", fontSize = 20.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Main Blending Playground Card
            ClayCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                cornerRadius = 32.dp,
                elevation = 10.dp,
                bgGradient = if (isDarkMode) {
                    Brush.verticalGradient(listOf(Color(0xFF262A56), Color(0xFF1E2142)))
                } else {
                    Brush.verticalGradient(listOf(Color.White, Color(0xFFFFF9E6)))
                },
                borderHighlightColor = if (isMerged) CorrectGreen else accentColor
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Header prompt
                    Text(
                        text = if (isMerged) "वाह! नया शब्द बन गया! 🎉" else "दोनों अक्षरों को मिलाकर शब्द बनाओ!",
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        color = if (isMerged) CorrectGreen else (if (isDarkMode) Color(0xFFFFD54F) else Color(0xFF37474F))
                    )

                    // 3. Characters Merging Visual Boxes (Bigger Size)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .scale(mergeScale),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Letter 1 Box
                        ClayCard(
                            modifier = Modifier.size(98.dp),
                            cornerRadius = 24.dp,
                            elevation = 8.dp,
                            bgGradient = Brush.verticalGradient(listOf(CandyYellow, CandyOrange))
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = currentBlend.char1,
                                    fontSize = 54.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        // Plus sign
                        Text(
                            text = currentBlend.operatorSymbol,
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Black,
                            color = accentColor
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        // Letter 2 Box
                        ClayCard(
                            modifier = Modifier.size(98.dp),
                            cornerRadius = 24.dp,
                            elevation = 8.dp,
                            bgGradient = Brush.verticalGradient(listOf(Color(0xFF40C4FF), Color(0xFF0091EA)))
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = currentBlend.char2,
                                    fontSize = 54.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // 4. Equal Sign & Result Word Box (Shown after merge)
                    if (isMerged) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "⬇️ बराबर (Equals) ⬇️", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = CorrectGreen)

                            Spacer(modifier = Modifier.height(8.dp))

                            // Resulting Word Display
                            ClayCard(
                                modifier = Modifier.fillMaxWidth(0.92f),
                                cornerRadius = 26.dp,
                                elevation = 8.dp,
                                bgGradient = Brush.verticalGradient(
                                    listOf(Color(0xFFE8F8F0), Color(0xFFC8E6C9))
                                ),
                                borderHighlightColor = CorrectGreen
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = currentBlend.emoji, fontSize = 76.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = currentBlend.resultWord,
                                        fontSize = 50.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF1B5E20)
                                    )
                                    Text(
                                        text = currentBlend.meaningHindi,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E7D32),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        // Merge Action Button
                        ClayButton(
                            onClick = {
                                isMerged = true
                                showConfetti = true
                                SoundEffectHelper.playCorrectChime()
                                ttsManager?.speak(currentBlend.audioExplanationHindi)
                                onWordBuilt(currentBlend.id)
                                onAddReward(30, 2)
                            },
                            modifier = Modifier.fillMaxWidth(0.85f),
                            primaryColor = CorrectGreen,
                            shadowColor = Color(0xFF1B5E20),
                            topHighlightColor = Color(0xFF69F0AE)
                        ) {
                            Text(
                                text = "जादू से जोड़ो ✨",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 19.sp,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }

                    // 5. Priya Didi Guidance
                    ClayCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 20.dp,
                        elevation = 4.dp,
                        bgGradient = Brush.horizontalGradient(
                            if (isDarkMode) listOf(Color(0xFF1A1D3A), Color(0xFF23274E))
                            else listOf(Color(0xFFFFFDE7), Color(0xFFFFF9C4))
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TeacherAvatar3D(
                                size = 88.dp,
                                pose = if (isMerged) TeacherPose.CHEERING_STARS else TeacherPose.POINTING_AT_CARD,
                                isSpeaking = isMerged
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = if (isMerged) {
                                    "“${currentBlend.audioExplanationHindi}”"
                                } else {
                                    "“जब हम '${currentBlend.char1}' और '${currentBlend.char2}' को साथ लाते हैं, तो एक नया शब्द बनता है!”"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkMode) Color(0xFFFFD54F) else Color(0xFF4E342E),
                                lineHeight = 17.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 6. Bottom Navigation Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (blendIndex > 0) {
                    ClayButton(
                        onClick = { blendIndex-- },
                        modifier = Modifier.weight(0.4f),
                        primaryColor = Color(0xFF78909C),
                        shadowColor = Color(0xFF455A64),
                        topHighlightColor = Color(0xFF90A4AE)
                    ) {
                        Text(
                            text = "◀ पिछला",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
                if (blendIndex < blends.size - 1) {
                    ClayButton(
                        onClick = {
                            blendIndex++
                        },
                        modifier = Modifier.weight(if (blendIndex > 0) 0.6f else 1f),
                        primaryColor = accentColor,
                        shadowColor = accentColor.copy(alpha = 0.7f),
                        topHighlightColor = accentColor.copy(alpha = 0.9f)
                    ) {
                        Text(
                            text = "अगला शब्द ▶",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                } else {
                    ClayButton(
                        onClick = onNavigateToQuiz,
                        modifier = Modifier.weight(if (blendIndex > 0) 0.6f else 1f),
                        primaryColor = CorrectGreen,
                        shadowColor = Color(0xFF27AE60),
                        topHighlightColor = Color(0xFF58D68D)
                    ) {
                        Text(
                            text = "लेवल 4: खेल 🎯",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }

        // Confetti celebration
        StarConfettiOverlay(
            trigger = showConfetti,
            onFinished = { showConfetti = false }
        )
    }
}

