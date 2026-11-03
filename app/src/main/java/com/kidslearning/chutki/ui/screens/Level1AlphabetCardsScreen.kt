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
import androidx.compose.ui.text.style.TextOverflow
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
import com.kidslearning.chutki.theme.GoldenStar
import com.kidslearning.chutki.theme.MintGreen
import com.kidslearning.chutki.ui.components.ClayButton
import com.kidslearning.chutki.ui.components.ClayCard
import com.kidslearning.chutki.ui.components.TeacherAvatar3D
import com.kidslearning.chutki.ui.components.TeacherPose

data class CardTheme(
    val accentColor: Color,
    val cardGradientLight: List<Color>,
    val cardGradientDark: List<Color>,
    val badgeBgLight: Color,
    val badgeBgDark: Color,
    val tutorBgLight: List<Color>,
    val tutorBgDark: List<Color>
)

val cardThemes = listOf(
    // 1. Sunshine Orange
    CardTheme(
        accentColor = Color(0xFFFF6D00),
        cardGradientLight = listOf(Color(0xFFFFFFFF), Color(0xFFFFF3E0)),
        cardGradientDark = listOf(Color(0xFF2E1C12), Color(0xFF1E140D)),
        badgeBgLight = Color(0xFFFFE0B2),
        badgeBgDark = Color(0xFF4E2C14),
        tutorBgLight = listOf(Color(0xFFFFF8E1), Color(0xFFFFECB3)),
        tutorBgDark = listOf(Color(0xFF2A2015), Color(0xFF1D170E))
    ),
    // 2. Sky Blue
    CardTheme(
        accentColor = Color(0xFF0091EA),
        cardGradientLight = listOf(Color(0xFFFFFFFF), Color(0xFFE1F5FE)),
        cardGradientDark = listOf(Color(0xFF10283A), Color(0xFF0A1B28)),
        badgeBgLight = Color(0xFFB3E5FC),
        badgeBgDark = Color(0xFF0F3652),
        tutorBgLight = listOf(Color(0xFFE0F7FA), Color(0xFFB2EBF2)),
        tutorBgDark = listOf(Color(0xFF0E2C33), Color(0xFF071F24))
    ),
    // 3. Bright Emerald
    CardTheme(
        accentColor = Color(0xFF00C853),
        cardGradientLight = listOf(Color(0xFFFFFFFF), Color(0xFFE8F5E9)),
        cardGradientDark = listOf(Color(0xFF122C1A), Color(0xFF0B1F12)),
        badgeBgLight = Color(0xFFC8E6C9),
        badgeBgDark = Color(0xFF1B4D27),
        tutorBgLight = listOf(Color(0xFFF1F8E9), Color(0xFFDCEDC8)),
        tutorBgDark = listOf(Color(0xFF1D3815), Color(0xFF12270D))
    ),
    // 4. Magic Purple
    CardTheme(
        accentColor = Color(0xFFAA00FF),
        cardGradientLight = listOf(Color(0xFFFFFFFF), Color(0xFFF3E5F5)),
        cardGradientDark = listOf(Color(0xFF2C1236), Color(0xFF1E0A26)),
        badgeBgLight = Color(0xFFE1BEE7),
        badgeBgDark = Color(0xFF4A185C),
        tutorBgLight = listOf(Color(0xFFEDE7F6), Color(0xFFD1C4E9)),
        tutorBgDark = listOf(Color(0xFF26183B), Color(0xFF170E26))
    ),
    // 5. Candy Red
    CardTheme(
        accentColor = Color(0xFFFF1744),
        cardGradientLight = listOf(Color(0xFFFFFFFF), Color(0xFFFCE4EC)),
        cardGradientDark = listOf(Color(0xFF33121C), Color(0xFF240A12)),
        badgeBgLight = Color(0xFFF8BBD0),
        badgeBgDark = Color(0xFF521527),
        tutorBgLight = listOf(Color(0xFFFFEBEE), Color(0xFFFFCDD2)),
        tutorBgDark = listOf(Color(0xFF381418), Color(0xFF240A0D))
    ),
    // 6. Honey Amber
    CardTheme(
        accentColor = Color(0xFFFFAB00),
        cardGradientLight = listOf(Color(0xFFFFFFFF), Color(0xFFFFF8E1)),
        cardGradientDark = listOf(Color(0xFF302410), Color(0xFF201809)),
        badgeBgLight = Color(0xFFFFECB3),
        badgeBgDark = Color(0xFF543E15),
        tutorBgLight = listOf(Color(0xFFFFFDE7), Color(0xFFFFF9C4)),
        tutorBgDark = listOf(Color(0xFF332F10), Color(0xFF242008))
    ),
    // 7. Ocean Turquoise
    CardTheme(
        accentColor = Color(0xFF00BFA5),
        cardGradientLight = listOf(Color(0xFFFFFFFF), Color(0xFFE0F2F1)),
        cardGradientDark = listOf(Color(0xFF0E2A27), Color(0xFF081C1A)),
        badgeBgLight = Color(0xFFB2DFDB),
        badgeBgDark = Color(0xFF134540),
        tutorBgLight = listOf(Color(0xFFE8F8F5), Color(0xFFD1F2EB)),
        tutorBgDark = listOf(Color(0xFF113833), Color(0xFF0A2622))
    ),
    // 8. Royal Blue
    CardTheme(
        accentColor = Color(0xFF3D5AFE),
        cardGradientLight = listOf(Color(0xFFFFFFFF), Color(0xFFE8EAF6)),
        cardGradientDark = listOf(Color(0xFF131A38), Color(0xFF0B1026)),
        badgeBgLight = Color(0xFFC5CAE9),
        badgeBgDark = Color(0xFF1D2859),
        tutorBgLight = listOf(Color(0xFFEDE7F6), Color(0xFFC5CAE9)),
        tutorBgDark = listOf(Color(0xFF191F47), Color(0xFF101433))
    ),
    // 9. Warm Tangerine
    CardTheme(
        accentColor = Color(0xFFFF3D00),
        cardGradientLight = listOf(Color(0xFFFFFFFF), Color(0xFFFBE9E7)),
        cardGradientDark = listOf(Color(0xFF33170F), Color(0xFF240E08)),
        badgeBgLight = Color(0xFFFFCCBC),
        badgeBgDark = Color(0xFF542213),
        tutorBgLight = listOf(Color(0xFFFFF3E0), Color(0xFFFFE0B2)),
        tutorBgDark = listOf(Color(0xFF3B1E12), Color(0xFF261209))
    ),
    // 10. Spring Lime
    CardTheme(
        accentColor = Color(0xFF76FF03),
        cardGradientLight = listOf(Color(0xFFFFFFFF), Color(0xFFF1F8E9)),
        cardGradientDark = listOf(Color(0xFF1B2E0D), Color(0xFF112006)),
        badgeBgLight = Color(0xFFDCEDC8),
        badgeBgDark = Color(0xFF2E4F14),
        tutorBgLight = listOf(Color(0xFFF9FBE7), Color(0xFFF0F4C3)),
        tutorBgDark = listOf(Color(0xFF29330C), Color(0xFF1C2406))
    ),
    // 11. Electric Indigo
    CardTheme(
        accentColor = Color(0xFF7C4DFF),
        cardGradientLight = listOf(Color(0xFFFFFFFF), Color(0xFFEDE7F6)),
        cardGradientDark = listOf(Color(0xFF21103A), Color(0xFF150826)),
        badgeBgLight = Color(0xFFD1C4E9),
        badgeBgDark = Color(0xFF381966),
        tutorBgLight = listOf(Color(0xFFF3E5F5), Color(0xFFE1BEE7)),
        tutorBgDark = listOf(Color(0xFF2C1347), Color(0xFF1D0A30))
    ),
    // 12. Vivid Berry Pink
    CardTheme(
        accentColor = Color(0xFFFF4081),
        cardGradientLight = listOf(Color(0xFFFFFFFF), Color(0xFFFCE4EC)),
        cardGradientDark = listOf(Color(0xFF31101F), Color(0xFF210813)),
        badgeBgLight = Color(0xFFF8BBD0),
        badgeBgDark = Color(0xFF521731),
        tutorBgLight = listOf(Color(0xFFFFF0F5), Color(0xFFFFD1DC)),
        tutorBgDark = listOf(Color(0xFF381524), Color(0xFF240B16))
    )
)

@Composable
fun Level1AlphabetCardsScreen(
    subject: SubjectType,
    isDarkMode: Boolean,
    ttsManager: HindiTtsManager?,
    userPrefs: UserPreferencesManager? = null,
    onBack: () -> Unit,
    onNavigateToLevel2: () -> Unit,
    onCardLearned: (cardId: String) -> Unit = {}
) {
    val cards = remember(subject.id) {
        LearningContentRepository.getAlphabetCards(subject.id)
    }

    val savedIndex = remember(subject.id) {
        userPrefs?.getLastCardIndex(subject.id) ?: 0
    }

    var currentIndex by remember(subject.id) {
        mutableIntStateOf(savedIndex.coerceIn(0, (cards.size - 1).coerceAtLeast(0)))
    }
    val currentCard = cards.getOrNull(currentIndex) ?: cards.first()
    val currentTheme = remember(currentIndex) {
        cardThemes[currentIndex % cardThemes.size]
    }

    // Trigger TTS speech, mark card learned & persist current index whenever card changes
    LaunchedEffect(currentIndex) {
        onCardLearned(currentCard.id)
        userPrefs?.setLastCardIndex(subject.id, currentIndex, "${currentCard.char} (${currentCard.wordHindi})")
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
                    color = currentTheme.accentColor
                )
            }

            // Audio Replay Button
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(currentTheme.accentColor)
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
            color = currentTheme.accentColor,
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
                bgGradient = Brush.verticalGradient(
                    if (isDarkMode) currentTheme.cardGradientDark else currentTheme.cardGradientLight
                ),
                borderHighlightColor = currentTheme.accentColor
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top Character Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isDarkMode) currentTheme.badgeBgDark else currentTheme.badgeBgLight)
                            .padding(horizontal = 14.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "उच्चारण (Sound): ${card.phonetic}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = currentTheme.accentColor
                        )
                    }

                    // 3D Character Display (Kid-friendly size, leaves ample room)
                    Text(
                        text = card.char,
                        fontWeight = FontWeight.Black,
                        fontSize = 76.sp,
                        color = currentTheme.accentColor
                    )

                    // Emoji Illustration
                    Text(
                        text = card.emoji,
                        fontSize = 62.sp
                    )

                    // Hindi & English Word Label in a snug, stylish pill container
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isDarkMode) currentTheme.badgeBgDark.copy(alpha = 0.5f)
                                else currentTheme.badgeBgLight.copy(alpha = 0.6f)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val hindiText = if (card.char.toIntOrNull() != null || subject == SubjectType.MATH) {
                                card.wordHindi
                            } else {
                                "${card.char} से ${card.wordHindi}"
                            }

                            Text(
                                text = hindiText,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = if (hindiText.length > 25) 16.sp else if (hindiText.length > 16) 18.sp else 21.sp,
                                color = if (isDarkMode) Color.White else Color(0xFF263238),
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (card.wordEnglish.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = card.wordEnglish,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = if (isDarkMode) Color(0xFFCFD8DC) else Color(0xFF455A64),
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // 3. Priya Didi Tutor Section pointing at card
                    ClayCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 20.dp,
                        elevation = 4.dp,
                        bgGradient = Brush.horizontalGradient(
                            if (isDarkMode) currentTheme.tutorBgDark else currentTheme.tutorBgLight
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TeacherAvatar3D(
                                size = 68.dp,
                                pose = TeacherPose.POINTING_AT_CARD,
                                isSpeaking = true,
                                onClick = {
                                    ttsManager?.speak(card.hindiAudioSentence)
                                }
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                text = "“${card.hindiAudioSentence}”",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkMode) Color(0xFFFFD54F) else Color(0xFF4E342E),
                                lineHeight = 18.sp,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
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
                val nextLabel = when (subject) {
                    SubjectType.MATH -> "अगली संख्या ▶"
                    SubjectType.ENGLISH -> "Next ▶"
                    else -> "अगला अक्षर ▶"
                }
                ClayButton(
                    onClick = {
                        currentIndex++
                    },
                    modifier = Modifier.weight(1f),
                    primaryColor = currentTheme.accentColor,
                    shadowColor = currentTheme.accentColor.copy(alpha = 0.7f),
                    topHighlightColor = currentTheme.accentColor.copy(alpha = 0.9f)
                ) {
                    Text(
                        text = nextLabel,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                }
            } else {
                // Completed all cards -> Go to Level 2 Tracing
                val level2Label = if (subject == SubjectType.MATH) "लेवल 2: संख्या लिखो ✍️" else "लेवल 2: लिखना सीखो ✍️"
                ClayButton(
                    onClick = onNavigateToLevel2,
                    modifier = Modifier.weight(1.3f),
                    primaryColor = CorrectGreen,
                    shadowColor = Color(0xFF27AE60),
                    topHighlightColor = Color(0xFF58D68D)
                ) {
                    Text(
                        text = level2Label,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
