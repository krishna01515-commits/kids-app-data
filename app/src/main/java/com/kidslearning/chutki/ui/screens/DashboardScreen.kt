package com.kidslearning.chutki.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidslearning.chutki.model.SubjectType
import com.kidslearning.chutki.model.UserProfile
import com.kidslearning.chutki.theme.CandyOrange
import com.kidslearning.chutki.theme.CandyYellow
import com.kidslearning.chutki.theme.GoldenStar
import com.kidslearning.chutki.theme.MintGreen
import com.kidslearning.chutki.theme.SkyBlue
import com.kidslearning.chutki.ui.components.ClayButton
import com.kidslearning.chutki.ui.components.ClayCard
import com.kidslearning.chutki.ui.components.StreakBadge
import com.kidslearning.chutki.ui.components.TeacherAvatar3D
import com.kidslearning.chutki.ui.components.TeacherPose

@Composable
fun DashboardScreen(
    userProfile: UserProfile,
    onSelectSubject: (SubjectType) -> Unit,
    onResumeLesson: () -> Unit,
    onToggleDarkMode: () -> Unit,
    onToggleLanguage: (String) -> Unit = {}
) {
    val isDark = userProfile.isDarkMode
    val isEn = userProfile.isEnglish

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (isDark) {
                    Brush.verticalGradient(listOf(Color(0xFF13142B), Color(0xFF1B1D3D)))
                } else {
                    Brush.verticalGradient(listOf(Color(0xFFFFFDF5), Color(0xFFFFF3E0)))
                }
            )
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) {
        // 1. Top Bar: Kid Avatar, Name, Streak, XP, Dark Mode Toggle
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Kid Info
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(CandyYellow, CandyOrange)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = userProfile.avatarEmoji, fontSize = 28.sp)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = if (isEn) "Hello, ${userProfile.name} 👋" else "नमस्ते, ${userProfile.name} 👋",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFFF0F2FF) else Color(0xFF2C3E50)
                        )
                        Text(
                            text = if (isEn) "Little Star • Age ${userProfile.age} yrs" else "नन्हे सितारे • उम्र ${userProfile.age} वर्ष",
                            fontSize = 12.sp,
                            color = if (isDark) Color(0xFFA5B0E0) else Color(0xFF78909C)
                        )
                    }
                }

                // Bilingual Flag Toggle, Dark Mode toggle & App Logo
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Bilingual Language Switcher Button (Flag & Code)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isDark) Color(0xFF262A56) else Color(0xFFFFECB3))
                            .clickable {
                                val nextLang = if (isEn) "hi" else "en"
                                onToggleLanguage(nextLang)
                            }
                            .padding(horizontal = 9.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isEn) "🇬🇧 EN" else "🇮🇳 हिं",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            color = if (isDark) Color.White else Color(0xFF3E2723)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.kidslearning.chutki.R.drawable.app_logo),
                        contentDescription = "Chutki Kids Logo",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF333866) else Color(0xFFFFECB3))
                            .clickable { onToggleDarkMode() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = if (isDark) "☀️" else "🌙", fontSize = 16.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // 2. Gamified Stats Row: Rank 🏆, Streak 🔥, XP ⭐, Stars 🌟
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Leaderboard Rank
                    ClayCard(
                        modifier = Modifier.weight(1f),
                        cornerRadius = 18.dp,
                        elevation = 4.dp,
                        bgGradient = Brush.horizontalGradient(
                            listOf(Color(0xFFFFEBEE), Color(0xFFFFCDD2))
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🏆", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Column {
                                Text(
                                    text = if (isEn) "Rank #${userProfile.currentRank}" else "रैंक #${userProfile.currentRank}",
                                    color = Color(0xFFC62828),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = if (isEn) "Leaderboard" else "लीडरबोर्ड",
                                    color = Color(0xFFE57373),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Streak
                    StreakBadge(
                        streakDays = userProfile.currentStreakDays,
                        modifier = Modifier.weight(1f)
                    )

                    // XP Points
                    ClayCard(
                        modifier = Modifier.weight(1f),
                        cornerRadius = 18.dp,
                        elevation = 4.dp,
                        bgGradient = Brush.horizontalGradient(
                            listOf(Color(0xFFFFF8E1), Color(0xFFFFECB3))
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "⭐", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Column {
                                Text(
                                    text = "${userProfile.totalXp} XP",
                                    color = Color(0xFFF57F17),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = if (isEn) "Points" else "कुल अंक",
                                    color = Color(0xFFFFB74D),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Stars count
                    ClayCard(
                        modifier = Modifier.weight(1f),
                        cornerRadius = 18.dp,
                        elevation = 4.dp,
                        bgGradient = Brush.horizontalGradient(
                            listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9))
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🌟", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Column {
                                Text(
                                    text = "${userProfile.totalStars} Stars",
                                    color = Color(0xFF2E7D32),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = if (isEn) "Stars" else "सितारे",
                                    color = Color(0xFF81C784),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }

        // 3. Current Course & Resume Lesson 3D Card (Dynamic Subject & Progress)
        item {
            val activeSubject = SubjectType.entries.find { it.id == userProfile.currentSubjectId } ?: SubjectType.HINDI
            val activeProgress = when (activeSubject) {
                SubjectType.HINDI -> userProfile.hindiProgressPercent
                SubjectType.ENGLISH -> userProfile.englishProgressPercent
                SubjectType.MATH -> userProfile.mathProgressPercent
            }
            val activeUnlockedLevel = when (activeSubject) {
                SubjectType.HINDI -> userProfile.hindiUnlockedLevel
                SubjectType.ENGLISH -> userProfile.englishUnlockedLevel
                SubjectType.MATH -> userProfile.mathUnlockedLevel
            }

            val subjectDisplayName = if (isEn) activeSubject.titleEnglish else activeSubject.titleHindi
            val levelPrefix = if (isEn) "Level" else "लेवल"

            ClayCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 28.dp,
                elevation = 8.dp,
                bgGradient = if (isDark) {
                    Brush.verticalGradient(listOf(Color(0xFF262A56), Color(0xFF1E2142)))
                } else {
                    Brush.verticalGradient(listOf(Color(0xFFFFECB3), Color(0xFFFFD54F)))
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(CandyOrange)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isEn) "Active Subject" else "सक्रिय विषय",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "$subjectDisplayName - $levelPrefix $activeUnlockedLevel",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = if (isDark) Color.White else Color(0xFF3E2723)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = if (isEn) "Progress: $activeProgress% complete" else "प्रगति: $activeProgress% पूरी हुई",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isDark) Color(0xFFA5B0E0) else Color(0xFF5D4037)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { (activeProgress / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            color = MintGreen,
                            trackColor = Color.White.copy(alpha = 0.5f)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        ClayButton(
                            onClick = onResumeLesson,
                            primaryColor = CandyOrange,
                            shadowColor = Color(0xFFBF360C),
                            topHighlightColor = Color(0xFFFFAB40)
                        ) {
                            Text(
                                text = if (isEn) "Continue ▶" else "आगे बढ़ो ▶",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }
                    }

                    // Priya Didi pointing (New 3D Model with Glasses & Stylus)
                    TeacherAvatar3D(
                        size = 135.dp,
                        pose = TeacherPose.POINTING_AT_CARD,
                        isSpeaking = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))
        }

        // 4. Section Title: Choose Subject / विषय चुनो
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEn) "📚 Select Subject (विषय चुनो)" else "📚 विषय चुनो (Select Subject)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = if (isDark) Color(0xFFF0F2FF) else Color(0xFF263238)
                )

                Text(
                    text = if (isEn) "3 Subjects" else "3 विषय उपलब्ध",
                    fontSize = 12.sp,
                    color = if (isDark) Color(0xFFA5B0E0) else Color(0xFF78909C)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // 5. Subject Cards: Hindi, English, Math
        items(SubjectType.entries.size) { index ->
            val subject = SubjectType.entries[index]

            val (cardGradient, accentColor, borderColor) = when (subject) {
                SubjectType.HINDI -> Triple(
                    Brush.verticalGradient(
                        if (isDark) listOf(Color(0xFF332115), Color(0xFF261910))
                        else listOf(Color(0xFFFFF3E0), Color(0xFFFFE0B2))
                    ),
                    CandyOrange,
                    Color(0xFFFFB74D)
                )
                SubjectType.ENGLISH -> Triple(
                    Brush.verticalGradient(
                        if (isDark) listOf(Color(0xFF132A3E), Color(0xFF0F1E2C))
                        else listOf(Color(0xFFE1F5FE), Color(0xFFB3E5FC))
                    ),
                    SkyBlue,
                    Color(0xFF4FC3F7)
                )
                SubjectType.MATH -> Triple(
                    Brush.verticalGradient(
                        if (isDark) listOf(Color(0xFF113222), Color(0xFF0C2418))
                        else listOf(Color(0xFFE8F8F0), Color(0xFFC8E6C9))
                    ),
                    MintGreen,
                    Color(0xFF81C784)
                )
            }

            ClayCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 7.dp)
                    .clickable { onSelectSubject(subject) },
                cornerRadius = 24.dp,
                elevation = 6.dp,
                bgGradient = cardGradient,
                borderHighlightColor = borderColor
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Big Subject Icon (Enlarged)
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(accentColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = subject.iconEmoji, fontSize = 44.sp)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Title & Description (Enlarged)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isEn) subject.titleEnglish else subject.titleHindi,
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp,
                            color = if (isDark) Color.White else Color(0xFF263238)
                        )
                        Text(
                            text = if (isEn) subject.titleHindi else subject.titleEnglish,
                            fontSize = 14.sp,
                            color = if (isDark) Color(0xFFA5B0E0) else Color(0xFF546E7A)
                        )

                        val subjectProgress = when (subject) {
                            SubjectType.HINDI -> userProfile.hindiProgressPercent
                            SubjectType.ENGLISH -> userProfile.englishProgressPercent
                            SubjectType.MATH -> userProfile.mathProgressPercent
                        }
                        val subjectUnlocked = when (subject) {
                            SubjectType.HINDI -> userProfile.hindiUnlockedLevel
                            SubjectType.ENGLISH -> userProfile.englishUnlockedLevel
                            SubjectType.MATH -> userProfile.mathUnlockedLevel
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isEn) "Progress: $subjectProgress%" else "प्रगति: $subjectProgress%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isEn) "• Level $subjectUnlocked Active" else "• लेवल $subjectUnlocked सक्रिय",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isDark) Color(0xFFA5B0E0) else Color(0xFF78909C)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        LinearProgressIndicator(
                            progress = { (subjectProgress / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = accentColor,
                            trackColor = if (isDark) Color(0xFF262A56) else Color(0xFFE0E0E0)
                        )
                    }

                    // Go Arrow Button
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(accentColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "➜", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

