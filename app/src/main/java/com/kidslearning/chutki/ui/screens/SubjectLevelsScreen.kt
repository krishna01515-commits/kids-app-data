package com.kidslearning.chutki.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidslearning.chutki.audio.HindiTtsManager
import com.kidslearning.chutki.audio.SoundEffectHelper
import com.kidslearning.chutki.data.LearningContentRepository
import com.kidslearning.chutki.data.UserPreferencesManager
import com.kidslearning.chutki.model.SubjectType
import com.kidslearning.chutki.model.UserProfile
import com.kidslearning.chutki.theme.CandyOrange
import com.kidslearning.chutki.theme.CorrectGreen
import com.kidslearning.chutki.ui.components.ClayCard
import com.kidslearning.chutki.ui.components.TeacherAvatar3D
import com.kidslearning.chutki.ui.components.TeacherPose

@Composable
fun SubjectLevelsScreen(
    subject: SubjectType,
    userProfile: UserProfile,
    userPrefs: UserPreferencesManager? = null,
    ttsManager: HindiTtsManager? = null,
    onBackToDashboard: () -> Unit,
    onOpenLevel: (levelNumber: Int) -> Unit
) {
    val isDarkMode = userProfile.isDarkMode

    val unlockedMax = when (subject) {
        SubjectType.HINDI -> userProfile.hindiUnlockedLevel
        SubjectType.ENGLISH -> userProfile.englishUnlockedLevel
        SubjectType.MATH -> userProfile.mathUnlockedLevel
    }

    val levels = remember(subject.id, unlockedMax, userProfile.totalXp) {
        LearningContentRepository.getLevelsForSubject(
            subjectId = subject.id,
            unlockedLevel = unlockedMax,
            getStars = { lvl -> userPrefs?.getLevelStars(subject.id, lvl) ?: if (lvl < unlockedMax) 3 else 1 },
            getPercent = { lvl -> userPrefs?.getLevelPercent(subject.id, lvl) ?: if (lvl < unlockedMax) 100 else 0 }
        )
    }

    val accentColor = Color(subject.themeColorHex)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (isDarkMode) Brush.verticalGradient(listOf(Color(0xFF13142B), Color(0xFF1B1D3D)))
                else Brush.verticalGradient(listOf(Color(0xFFFFFDF5), Color(0xFFFFF8E1)))
            )
            .padding(18.dp)
    ) {
        // 1. Header with Back Button and Subject Banner
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isDarkMode) Color(0xFF262A56) else Color(0xFFFFECB3))
                        .clickable { onBackToDashboard() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "⬅️", fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = if (userProfile.isEnglish) subject.titleEnglish else subject.titleHindi,
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp,
                        color = if (isDarkMode) Color.White else Color(0xFF263238)
                    )
                    Text(
                        text = if (userProfile.isEnglish) {
                            "Learning Path (Levels 1 to 8) • Progress: ${
                                when (subject) {
                                    SubjectType.HINDI -> userProfile.hindiProgressPercent
                                    SubjectType.ENGLISH -> userProfile.englishProgressPercent
                                    SubjectType.MATH -> userProfile.mathProgressPercent
                                }
                            }%"
                        } else {
                            "सीखने का मार्ग (लेवल 1 से 8) • प्रगति: ${
                                when (subject) {
                                    SubjectType.HINDI -> userProfile.hindiProgressPercent
                                    SubjectType.ENGLISH -> userProfile.englishProgressPercent
                                    SubjectType.MATH -> userProfile.mathProgressPercent
                                }
                            }%"
                        },
                        fontSize = 13.sp,
                        color = if (isDarkMode) Color(0xFFA5B0E0) else Color(0xFF78909C)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // 2. Teacher Encouragement Banner
        item {
            ClayCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp,
                elevation = 6.dp,
                bgGradient = Brush.horizontalGradient(
                    if (isDarkMode) listOf(Color(0xFF262A56), Color(0xFF1E2142))
                    else listOf(Color(0xFFFFECB3), Color(0xFFFFD54F))
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TeacherAvatar3D(
                        size = 110.dp,
                        pose = TeacherPose.POINTING_AT_CARD,
                        isSpeaking = false
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = if (userProfile.isEnglish) "Teacher Priya says:" else "प्रिया दीदी कहती हैं:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = CandyOrange
                        )
                        Text(
                            text = if (userProfile.isEnglish) "“Pass each level to unlock the next exciting adventure! You are brilliant!”" else "“एक-एक लेवल पार करो और नए लेवल अनलॉक करो! आप बहुत होशियार हैं!”",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDarkMode) Color.White else Color(0xFF3E2723)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // 3. Level Nodes (Sequential Levels Map)
        items(levels.size) { index ->
            val level = levels[index]
            val isUnlocked = level.isUnlocked

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Connecting vertical line if not first
                if (index > 0) {
                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .height(26.dp)
                            .background(
                                if (isUnlocked) accentColor.copy(alpha = 0.8f) else Color.LightGray,
                                shape = RoundedCornerShape(3.dp)
                            )
                    )
                }

                // Level Card
                ClayCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (isUnlocked) {
                                SoundEffectHelper.playPopSound()
                                onOpenLevel(level.levelNumber)
                            } else {
                                SoundEffectHelper.playWrongBoing()
                                if (userProfile.isEnglish) {
                                    ttsManager?.speak("This level is locked! Please complete level ${level.levelNumber - 1} first!")
                                } else {
                                    ttsManager?.speak("यह लेवल अभी बंद है! पहले लेवल ${level.levelNumber - 1} पूरा करें!")
                                }
                            }
                        },
                    cornerRadius = 24.dp,
                    elevation = if (isUnlocked) 8.dp else 2.dp,
                    bgGradient = when {
                        !isUnlocked -> Brush.verticalGradient(
                            listOf(Color(0xFFECEFF1), Color(0xFFCFD8DC))
                        )
                        isDarkMode -> Brush.verticalGradient(
                            listOf(Color(0xFF1E2142), Color(0xFF262A56))
                        )
                        else -> Brush.verticalGradient(
                            listOf(Color.White, Color(0xFFFFF9E6))
                        )
                    },
                    borderHighlightColor = if (isUnlocked) accentColor else Color.LightGray
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Level Number Icon Badge (Enlarged)
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isUnlocked) {
                                        Brush.radialGradient(listOf(accentColor, accentColor.copy(alpha = 0.8f)))
                                    } else {
                                        Brush.radialGradient(listOf(Color.Gray, Color.DarkGray))
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isUnlocked) {
                                Text(text = level.iconEmoji, fontSize = 34.sp)
                            } else {
                                Text(text = "🔒", fontSize = 28.sp)
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Title & Progress (Enlarged)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = level.titleHindi,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = if (isUnlocked) {
                                    if (isDarkMode) Color.White else Color(0xFF263238)
                                } else {
                                    Color.Gray
                                }
                            )

                            Text(
                                text = level.subtitleHindi,
                                fontSize = 12.sp,
                                color = if (isDarkMode) Color(0xFFA5B0E0) else Color(0xFF78909C)
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Stars earned & completion bar
                            if (isUnlocked) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    repeat(3) { starIndex ->
                                        Text(
                                            text = if (starIndex < level.starsEarned) "⭐" else "☆",
                                            fontSize = 15.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "+${level.xpReward} XP",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CandyOrange
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${level.completionPercent}% पूर्ण",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = CorrectGreen
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { (level.completionPercent / 100f).coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth(0.9f)
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = accentColor,
                                    trackColor = if (isDarkMode) Color(0xFF1A1D36) else Color(0xFFE0E0E0)
                                )
                            } else {
                                Text(
                                    text = "🔒 लेवल ${level.levelNumber - 1} पूरा करके अनलॉक करें",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Gray
                                )
                            }
                        }

                        // Play / Arrow button
                        if (isUnlocked) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(CorrectGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "▶", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
