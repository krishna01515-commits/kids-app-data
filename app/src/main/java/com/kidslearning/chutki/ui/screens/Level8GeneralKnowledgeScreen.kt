package com.kidslearning.chutki.ui.screens

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.kidslearning.chutki.data.GkCard
import com.kidslearning.chutki.data.GkCategory
import com.kidslearning.chutki.data.LearningContentRepository
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

@Composable
fun Level8GeneralKnowledgeScreen(
    subject: SubjectType,
    userProfile: UserProfile,
    userPrefs: UserPreferencesManager? = null,
    ttsManager: HindiTtsManager? = null,
    onBack: () -> Unit = {}
) {
    val isDarkMode = userProfile.isDarkMode
    val isEnglish = userProfile.isEnglish
    val allCards = remember { LearningContentRepository.getGkCards() }

    var selectedCategory by remember { mutableStateOf(GkCategory.ANIMAL) }
    val filteredCards = remember(selectedCategory) {
        allCards.filter { it.category == selectedCategory }
    }

    val exploredCardIds = remember { mutableStateListOf<String>() }
    var activeSpeakingCardId by remember { mutableStateOf<String?>(null) }
    var showConfetti by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "gk_anim")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // Save auto-resume
    LaunchedEffect(selectedCategory) {
        userPrefs?.recordLastActiveProgram(
            type = "LEVEL_8_GENERAL_KNOWLEDGE",
            subjectId = subject.id,
            levelNumber = 8,
            itemLabel = when (selectedCategory) {
                GkCategory.ANIMAL -> "जानवर और आवाजें 🦁"
                GkCategory.BODY_PART -> "शरीर के अंग 👁️"
                GkCategory.FRUIT_VEGETABLE -> "फल व सब्जियाँ 🍎"
            }
        )
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
                        text = if (isEnglish) "Level 8: General Knowledge Fun 🌍" else "लेवल 8: दुनिया की पहचान 🌍",
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        color = if (isDarkMode) Color.White else Color(0xFF263238)
                    )
                    Text(
                        text = when (selectedCategory) {
                            GkCategory.ANIMAL -> if (isEnglish) "Animals & Their Sounds 🦁" else "जानवर और उनकी आवाजें 🦁"
                            GkCategory.BODY_PART -> if (isEnglish) "My Body Parts 👁️" else "हमारे शरीर के अंग 👁️"
                            GkCategory.FRUIT_VEGETABLE -> if (isEnglish) "Yummy Fruits & Veggies 🍎" else "फल और सब्जियाँ 🍎"
                        },
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

            // --- 2. Category Filter Tabs ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Triple(GkCategory.ANIMAL, "जानवर 🦁", "Animals 🦁"),
                    Triple(GkCategory.BODY_PART, "अंग 👁️", "Body 👁️"),
                    Triple(GkCategory.FRUIT_VEGETABLE, "फल-सब्जी 🍎", "Fruits 🍎")
                ).forEach { (cat, titleHi, titleEn) ->
                    val isSelected = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) CandyOrange
                                else if (isDarkMode) Color(0xFF262A56)
                                else Color(0xFFECEFF1)
                            )
                            .clickable {
                                selectedCategory = cat
                                SoundEffectHelper.playPopSound()
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isEnglish) titleEn else titleHi,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                            color = if (isSelected) Color.White else if (isDarkMode) Color.LightGray else Color(0xFF37474F)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // --- 3. Interactive Cards Grid (2 Columns) ---
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val rowCount = (filteredCards.size + 1) / 2
                for (rowIndex in 0 until rowCount) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (colIndex in 0 until 2) {
                            val cardIdx = rowIndex * 2 + colIndex
                            if (cardIdx < filteredCards.size) {
                                val card = filteredCards[cardIdx]
                                val isExplored = exploredCardIds.contains(card.id)
                                val isSpeaking = activeSpeakingCardId == card.id

                                ClayCard(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            activeSpeakingCardId = card.id
                                            if (!isExplored) {
                                                exploredCardIds.add(card.id)
                                                SoundEffectHelper.playCoinSound()
                                                userPrefs?.recordGkActivity(subject.id, card.id)
                                                if (exploredCardIds.size >= 5) {
                                                    showConfetti = true
                                                    SoundEffectHelper.playStarTwinkle()
                                                }
                                            } else {
                                                SoundEffectHelper.playPopSound()
                                            }

                                            // Speak with Priya Didi
                                            val speech = if (isEnglish) {
                                                "${card.nameEnglish}! ${card.soundOrRoleEnglish}. ${card.funFactEnglish}"
                                            } else {
                                                "${card.nameHindi}! आवाज या काम: ${card.soundOrRoleHindi}। ${card.funFactHindi}"
                                            }
                                            ttsManager?.speak(speech)
                                        },
                                    cornerRadius = 22.dp,
                                    bgGradient = Brush.verticalGradient(
                                        if (isSpeaking) listOf(Color(0xFFFFF9C4), Color(0xFFFFE082))
                                        else if (isExplored) listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9))
                                        else if (isDarkMode) listOf(Color(0xFF1E2142), Color(0xFF262A56))
                                        else listOf(Color.White, Color(0xFFFFFDF5))
                                    ),
                                    borderHighlightColor = if (isSpeaking) CandyOrange else if (isExplored) CorrectGreen else Color.Transparent
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        // Emoji Illustration
                                        Box(
                                            modifier = Modifier
                                                .size(62.dp)
                                                .scale(if (isSpeaking) pulseScale else 1f)
                                                .clip(CircleShape)
                                                .background(CandyOrange.copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = card.emoji, fontSize = 34.sp)
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Names
                                        Text(
                                            text = if (isEnglish) card.nameEnglish else card.nameHindi,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (isDarkMode && !isExplored && !isSpeaking) Color.White else Color(0xFF263238)
                                        )

                                        Text(
                                            text = if (isEnglish) card.nameHindi else card.nameEnglish,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = CandyOrange
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        // Sound or Role Tag
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color(0x15000000))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = if (isEnglish) card.soundOrRoleEnglish else card.soundOrRoleHindi,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isDarkMode && !isExplored && !isSpeaking) Color.LightGray else Color(0xFF455A64),
                                                textAlign = TextAlign.Center
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        // Tap to Hear prompt
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = if (isSpeaking) "🔊 बोल रहे हैं..." else "👉 छूकर सुनो",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSpeaking) Color(0xFFD84315) else CorrectGreen
                                            )
                                        }
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 4. Teacher Priya Didi GK Guide Banner ---
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
                        pose = TeacherPose.POINTING_AT_CARD,
                        isSpeaking = false
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
                            text = if (isEnglish) "“Each card has an interesting voice & fact! Tap all cards to earn your Junior Genius badge!”"
                            else "“हर कार्ड में एक मजेदार आवाज और सीख है! सभी कार्ड्स छूकर सुनो और नन्हे जीनियस बनो!”",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDarkMode) Color.White else Color(0xFF3E2723)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // --- 5. Back to Subject Levels Button ---
            ClayButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                primaryColor = CorrectGreen
            ) {
                Text(
                    text = if (isEnglish) "🏆 Complete & Return to Levels" else "🏆 पूरा हुआ! लेवल सूची पर लौटें ⬅️",
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

