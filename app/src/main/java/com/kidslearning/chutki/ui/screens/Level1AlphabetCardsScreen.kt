package com.kidslearning.chutki.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidslearning.chutki.audio.HindiTtsManager
import com.kidslearning.chutki.audio.SoundEffectHelper
import com.kidslearning.chutki.data.LearningContentRepository
import com.kidslearning.chutki.model.SubjectType
import com.kidslearning.chutki.theme.CandyOrange
import com.kidslearning.chutki.theme.CandyYellow
import com.kidslearning.chutki.theme.CorrectGreen
import com.kidslearning.chutki.theme.GoldenStar
import com.kidslearning.chutki.theme.MintGreen
import com.kidslearning.chutki.ui.components.ClayButton
import com.kidslearning.chutki.ui.components.ClayCard
import com.kidslearning.chutki.ui.components.TeacherAvatar3D
import com.kidslearning.chutki.ui.components.TeacherPose

@Composable
fun Level1AlphabetCardsScreen(
    subject: SubjectType,
    isDarkMode: Boolean,
    ttsManager: HindiTtsManager?,
    onBack: () -> Unit,
    onNavigateToLevel2: () -> Unit,
    onCardLearned: (cardId: String) -> Unit = {}
) {
    val cards = remember(subject.id) {
        LearningContentRepository.getAlphabetCards(subject.id)
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    val currentCard = cards.getOrNull(currentIndex) ?: cards.first()
    val accentColor = Color(subject.themeColorHex)

    // Trigger TTS speech & mark card learned whenever card changes
    LaunchedEffect(currentIndex) {
        onCardLearned(currentCard.id)
        ttsManager?.speak(currentCard.hindiAudioSentence)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (isDarkMode) Brush.verticalGradient(listOf(Color(0xFF13142B), Color(0xFF1E2142)))
                else Brush.verticalGradient(listOf(Color(0xFFFFFDF5), Color(0xFFFFF3E0)))
            )
            .padding(18.dp),
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
                    text = "${subject.titleHindi} - लेवल 1",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = if (isDarkMode) Color.White else Color(0xFF263238)
                )
                Text(
                    text = "कार्ड ${currentIndex + 1} / ${cards.size}",
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
                        SoundEffectHelper.playPopSound()
                        ttsManager?.speak(currentCard.hindiAudioSentence)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🔊", fontSize = 20.sp)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Progress Bar
        LinearProgressIndicator(
            progress = { (currentIndex + 1).toFloat() / cards.size },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = accentColor,
            trackColor = Color.LightGray.copy(alpha = 0.4f)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Main Alphabet Card with animated transitions
        AnimatedContent(
            targetState = currentCard,
            transitionSpec = {
                (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                    slideOutHorizontally { width -> -width } + fadeOut()
                )
            },
            label = "card_animation",
            modifier = Modifier.weight(1f)
        ) { card ->
            ClayCard(
                modifier = Modifier.fillMaxSize(),
                cornerRadius = 32.dp,
                elevation = 10.dp,
                bgGradient = if (isDarkMode) {
                    Brush.verticalGradient(listOf(Color(0xFF262A56), Color(0xFF1E2142)))
                } else {
                    Brush.verticalGradient(listOf(Color.White, Color(0xFFFFF9E6)))
                },
                borderHighlightColor = accentColor
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top Character Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(accentColor.copy(alpha = 0.15f))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "उच्चारण (Sound): ${card.phonetic}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = accentColor
                        )
                    }

                    // Huge 3D Character Display (Bigger Size for Kids)
                    Text(
                        text = card.char,
                        fontWeight = FontWeight.Black,
                        fontSize = 115.sp,
                        color = accentColor
                    )

                    // Large Emoji Illustration
                    Text(
                        text = card.emoji,
                        fontSize = 90.sp
                    )

                    // Hindi & English Word Label
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${card.char} से ${card.wordHindi}",
                            fontWeight = FontWeight.Black,
                            fontSize = 32.sp,
                            color = if (isDarkMode) Color.White else Color(0xFF263238)
                        )
                        Text(
                            text = card.wordEnglish,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = if (isDarkMode) Color(0xFFA5B0E0) else Color(0xFF546E7A)
                        )
                    }

                    // 3. Priya Didi Tutor Section pointing at card
                    ClayCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 22.dp,
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
                                size = 95.dp,
                                pose = TeacherPose.POINTING_AT_CARD,
                                isSpeaking = true,
                                onClick = {
                                    ttsManager?.speak(card.hindiAudioSentence)
                                }
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = "“${card.hindiAudioSentence}”",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkMode) Color(0xFFFFD54F) else Color(0xFF4E342E),
                                lineHeight = 19.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Bottom Navigation Buttons (Previous & Next)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ClayButton(
                onClick = {
                    if (currentIndex > 0) {
                        currentIndex--
                    }
                },
                enabled = currentIndex > 0,
                modifier = Modifier.weight(1f),
                primaryColor = Color(0xFF78909C),
                shadowColor = Color(0xFF455A64),
                topHighlightColor = Color(0xFFB0BEC5)
            ) {
                Text(
                    text = "◀ पिछला",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            if (currentIndex < cards.size - 1) {
                ClayButton(
                    onClick = {
                        currentIndex++
                    },
                    modifier = Modifier.weight(1f),
                    primaryColor = accentColor,
                    shadowColor = accentColor.copy(alpha = 0.7f),
                    topHighlightColor = accentColor.copy(alpha = 0.9f)
                ) {
                    Text(
                        text = "अगला अक्षर ▶",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                }
            } else {
                // Completed all cards -> Go to Level 2 Tracing
                ClayButton(
                    onClick = onNavigateToLevel2,
                    modifier = Modifier.weight(1.3f),
                    primaryColor = CorrectGreen,
                    shadowColor = Color(0xFF27AE60),
                    topHighlightColor = Color(0xFF58D68D)
                ) {
                    Text(
                        text = "लेवल 2: लिखना सीखो ✍️",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
