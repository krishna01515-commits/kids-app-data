package com.kidslearning.chutki.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.kidslearning.chutki.data.StoryRhyme
import com.kidslearning.chutki.data.UserPreferencesManager
import com.kidslearning.chutki.model.SubjectType
import com.kidslearning.chutki.model.UserProfile
import com.kidslearning.chutki.theme.CandyOrange
import com.kidslearning.chutki.theme.CorrectGreen
import com.kidslearning.chutki.ui.components.ClayButton
import com.kidslearning.chutki.ui.components.ClayCard
import com.kidslearning.chutki.ui.components.KidAvatarView
import com.kidslearning.chutki.ui.components.StarConfettiOverlay
import com.kidslearning.chutki.ui.components.TeacherAvatar3D
import com.kidslearning.chutki.ui.components.TeacherPose
import kotlinx.coroutines.delay

@Composable
fun Level6StoriesRhymesScreen(
    subject: SubjectType,
    userProfile: UserProfile,
    userPrefs: UserPreferencesManager? = null,
    ttsManager: HindiTtsManager? = null,
    onBack: () -> Unit = {},
    onNavigateToLevel7: () -> Unit = {}
) {
    val isDarkMode = userProfile.isDarkMode
    val isEnglish = userProfile.isEnglish
    val rhymes = remember(subject.id) { LearningContentRepository.getStoriesAndRhymes(subject.id) }
    var selectedIndex by remember { mutableIntStateOf(0) }
    val currentRhyme = rhymes[selectedIndex]

    var activeLineIndex by remember { mutableIntStateOf(-1) }
    var isPlayingAudio by remember { mutableStateOf(false) }
    var showConfetti by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val characterScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "char_pulse"
    )

    // Save auto-resume state and welcome instruction
    LaunchedEffect(selectedIndex) {
        activeLineIndex = -1
        isPlayingAudio = false
        userPrefs?.recordLastActiveProgram(
            type = "LEVEL_6_STORIES_RHYMES",
            subjectId = subject.id,
            levelNumber = 6,
            itemLabel = currentRhyme.titleHindi
        )
    }

    // Line-by-line Karaoke audio playback loop
    LaunchedEffect(isPlayingAudio, selectedIndex) {
        if (isPlayingAudio) {
            val lines = if (isEnglish) currentRhyme.linesEnglish else currentRhyme.linesHindi
            for (i in lines.indices) {
                activeLineIndex = i
                ttsManager?.speak(lines[i])
                // Delay roughly proportional to line length
                val wordCount = lines[i].split(" ").size
                delay((wordCount * 450L).coerceAtLeast(2200L))
            }
            activeLineIndex = -1
            isPlayingAudio = false
            showConfetti = true
            SoundEffectHelper.playStarTwinkle()
            userPrefs?.recordStoryCompleted(subject.id, currentRhyme.id)
            if (isEnglish) {
                ttsManager?.speak("Wonderful singing! You completed this rhyme!")
            } else {
                ttsManager?.speak("शाबाश! आपने यह कविता पूरी सुन ली! बहुत अच्छे!")
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (isDarkMode) Brush.verticalGradient(listOf(Color(0xFF0F1226), Color(0xFF191D3C)))
                else Brush.verticalGradient(listOf(Color(0xFFFFF9EE), Color(0xFFFFF0D0)))
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- 1. Top Bar ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = {
                        isPlayingAudio = false
                        ttsManager?.stop()
                        onBack()
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isDarkMode) Color(0xFF262A56) else Color(0xFFFFECB3))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = if (isDarkMode) Color.White else Color(0xFF37474F)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isEnglish) "Level 6: Golden Stories & Rhymes 📖" else "लेवल 6: सुनहरी कहानियाँ व राइम्स 📖",
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        color = if (isDarkMode) Color.White else Color(0xFF263238)
                    )
                    Text(
                        text = if (isEnglish) "Rhyme ${selectedIndex + 1} of ${rhymes.size}" else "कविता ${selectedIndex + 1} / ${rhymes.size}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CandyOrange
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(CandyOrange.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    KidAvatarView(avatarKey = userProfile.avatarEmoji, size = 36.dp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- 2. Rhyme Selector Carousel Chips ---
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                itemsIndexed(rhymes) { idx, item ->
                    val isSelected = idx == selectedIndex
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) CandyOrange
                                else if (isDarkMode) Color(0xFF262A56)
                                else Color(0xFFECEFF1)
                            )
                            .clickable {
                                isPlayingAudio = false
                                ttsManager?.stop()
                                selectedIndex = idx
                                SoundEffectHelper.playPopSound()
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = item.emoji, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isEnglish) item.titleEnglish else item.titleHindi,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                                color = if (isSelected) Color.White else if (isDarkMode) Color.LightGray else Color(0xFF37474F)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // --- 3. Main Poem / Rhyme Karaoke Card ---
            ClayCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 26.dp,
                bgGradient = Brush.verticalGradient(
                    if (isDarkMode) listOf(Color(0xFF1E2142), Color(0xFF262A56))
                    else listOf(Color.White, Color(0xFFFFFDE7))
                ),
                borderHighlightColor = Color(0xFFFFD54F)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Title and Animated Illustration Character
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isEnglish) currentRhyme.titleEnglish else currentRhyme.titleHindi,
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                color = CandyOrange
                            )
                            Text(
                                text = if (isEnglish) "Sing along with Priya Didi!" else "प्रिया दीदी के साथ गाओ!",
                                fontSize = 12.sp,
                                color = if (isDarkMode) Color(0xFFA5B0E0) else Color(0xFF78909C)
                            )
                        }

                        // Big Bouncing Rhyme Emoji
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .scale(characterScale)
                                .clip(CircleShape)
                                .background(CandyOrange.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = currentRhyme.emoji, fontSize = 36.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4-Line Poem with Live Karaoke Highlight
                    val lines = if (isEnglish) currentRhyme.linesEnglish else currentRhyme.linesHindi
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        lines.forEachIndexed { index, line ->
                            val isHighlighted = activeLineIndex == index

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (isHighlighted) Color(0xFFFFD54F).copy(alpha = 0.35f)
                                        else if (isDarkMode) Color(0xFF262A56).copy(alpha = 0.5f)
                                        else Color(0xFFF1F5F9)
                                    )
                                    .border(
                                        width = if (isHighlighted) 2.dp else 0.dp,
                                        color = if (isHighlighted) Color(0xFFFFB300) else Color.Transparent,
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable {
                                        activeLineIndex = index
                                        ttsManager?.speak(line)
                                    }
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (isHighlighted) "🎵" else "⭐",
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = line,
                                        fontSize = 15.sp,
                                        fontWeight = if (isHighlighted) FontWeight.Black else FontWeight.Bold,
                                        color = if (isHighlighted) CandyOrange
                                        else if (isDarkMode) Color.White
                                        else Color(0xFF263238),
                                        lineHeight = 22.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Moral / Learning Fact Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(CorrectGreen.copy(alpha = 0.12f))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "💡", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isEnglish) currentRhyme.moralEnglish else currentRhyme.moralHindi,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDarkMode) Color(0xFF81C784) else Color(0xFF2E7D32)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 4. Controls: Play/Stop & Navigation ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ClayButton(
                    onClick = {
                        if (isPlayingAudio) {
                            isPlayingAudio = false
                            ttsManager?.stop()
                            activeLineIndex = -1
                        } else {
                            isPlayingAudio = true
                            SoundEffectHelper.playPopSound()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    primaryColor = if (isPlayingAudio) Color(0xFFFF5252) else CorrectGreen
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = if (isPlayingAudio) "⏸️" else "▶️", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isPlayingAudio) (if (isEnglish) "Stop" else "रोको") else (if (isEnglish) "Sing Along" else "सुनो और गाओ"),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }

                ClayButton(
                    onClick = {
                        isPlayingAudio = false
                        ttsManager?.stop()
                        if (selectedIndex < rhymes.size - 1) {
                            selectedIndex++
                        } else {
                            selectedIndex = 0
                        }
                        SoundEffectHelper.playPopSound()
                    },
                    modifier = Modifier.weight(1f),
                    primaryColor = Color(0xFF673AB7)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isEnglish) "Next Rhyme" else "अगली कविता",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Teacher Priya Didi Encouragement Card
            ClayCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp,
                bgGradient = Brush.horizontalGradient(
                    if (isDarkMode) listOf(Color(0xFF262A56), Color(0xFF1E2142))
                    else listOf(Color(0xFFFFECB3), Color(0xFFFFD54F))
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TeacherAvatar3D(
                        size = 80.dp,
                        pose = if (isPlayingAudio) TeacherPose.CHEERING_STARS else TeacherPose.POINTING_AT_CARD,
                        isSpeaking = isPlayingAudio
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = if (isEnglish) "Teacher Priya says:" else "प्रिया दीदी कहती हैं:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CandyOrange
                        )
                        Text(
                            text = if (isEnglish) "“Singing rhymes builds phonics and vocabulary! Keep singing, little star!”"
                            else "“कविताओं से भाषा और लय की समझ मजबूत होती है! मेरे साथ गुनगुनाओ नन्हे उस्ताद!”",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDarkMode) Color.White else Color(0xFF3E2723)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Unlock Next Level 7 Button
            ClayButton(
                onClick = onNavigateToLevel7,
                modifier = Modifier.fillMaxWidth(),
                primaryColor = CandyOrange
            ) {
                Text(
                    text = if (isEnglish) "Go to Level 7: Colors & Shapes 🎨" else "लेवल 7: रंग और आकार पर चलें 🎨 ➡️",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }

        StarConfettiOverlay(
            trigger = showConfetti,
            onFinished = { showConfetti = false }
        )
    }
}

