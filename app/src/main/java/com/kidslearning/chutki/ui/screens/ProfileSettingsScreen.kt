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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.kidslearning.chutki.model.UserProfile
import com.kidslearning.chutki.theme.CandyOrange
import com.kidslearning.chutki.theme.CandyYellow
import com.kidslearning.chutki.theme.CorrectGreen
import com.kidslearning.chutki.ui.components.ClayButton
import com.kidslearning.chutki.ui.components.ClayCard

@Composable
fun ProfileSettingsScreen(
    userProfile: UserProfile,
    isDarkMode: Boolean,
    ttsManager: HindiTtsManager? = null,
    onToggleDarkMode: (Boolean) -> Unit,
    onToggleLanguage: (String) -> Unit = {},
    onUpdateProfile: (name: String, avatar: String, age: Int) -> Unit,
    onLoginParent: (contact: String) -> Unit,
    onLogoutParent: () -> Unit,
    onResetProgress: () -> Unit
) {
    // Child profile editing state
    var childNameInput by remember(userProfile.name) { mutableStateOf(userProfile.name) }
    var selectedAvatar by remember(userProfile.avatarEmoji) { mutableStateOf(userProfile.avatarEmoji) }
    var selectedAge by remember(userProfile.age) { mutableIntStateOf(userProfile.age) }
    var profileSavedBanner by remember { mutableStateOf("") }
    var resetSuccessBanner by remember { mutableStateOf("") }

    // Parent login dialog state
    var showLoginDialog by remember { mutableStateOf(false) }
    var parentContactInput by remember { mutableStateOf(userProfile.parentEmailOrPhone) }
    var parentGateAnswer by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf("") }

    // Dynamic Verification Math Gate numbers (changes EVERY time, NO examples)
    var gateNum1 by remember { mutableIntStateOf(5) }
    var gateNum2 by remember { mutableIntStateOf(4) }

    fun generateNewGateQuestion() {
        gateNum1 = (4..12).random()
        gateNum2 = (3..9).random()
        parentGateAnswer = ""
        loginError = ""
    }

    // Reset confirmation dialog state
    var showResetDialog by remember { mutableStateOf(false) }

    val avatars = listOf("🦁", "👧", "👦", "🚀", "🐱", "🤖", "🦄", "🐼")
    val ageOptions = listOf(3, 4, 5, 6, 7, 8, 9, 10)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (isDarkMode) Brush.verticalGradient(listOf(Color(0xFF13142B), Color(0xFF1E2142)))
                else Brush.verticalGradient(listOf(Color(0xFFFFFDF5), Color(0xFFFFF3E0)))
            )
            .padding(16.dp)
    ) {
        // 1. Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (userProfile.isEnglish) "👤 Student Profile & Settings" else "👤 विद्यार्थी प्रोफाइल व सेटिंग्स",
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp,
                    color = if (isDarkMode) Color.White else Color(0xFF263238)
                )
                Text(
                    text = if (userProfile.isEnglish) "Edit profile, parent portal & preferences" else "नाम अपडेट, माता-पिता लॉगिन व प्रोग्रेस सेविंग",
                    fontSize = 13.sp,
                    color = if (isDarkMode) Color(0xFFA5B0E0) else Color(0xFF78909C)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        // 2. Status Banners (Save success / Reset success)
        item {
            if (profileSavedBanner.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFE8F5E9))
                        .padding(12.dp)
                ) {
                    Text(
                        text = profileSavedBanner,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            if (resetSuccessBanner.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFFFF3E0))
                        .padding(12.dp)
                ) {
                    Text(
                        text = resetSuccessBanner,
                        color = Color(0xFFE65100),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // 3. Child Profile Card (Editable Name, Avatar, Age)
        item {
            ClayCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 26.dp,
                elevation = 8.dp,
                bgGradient = if (isDarkMode) {
                    Brush.verticalGradient(listOf(Color(0xFF262A56), Color(0xFF1E2142)))
                } else {
                    Brush.verticalGradient(listOf(Color.White, Color(0xFFFFF9E6)))
                },
                borderHighlightColor = CandyOrange
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Big Avatar Circle
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(listOf(CandyYellow, CandyOrange))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = selectedAvatar, fontSize = 42.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Change Avatar Row
                    Text(
                        text = if (userProfile.isEnglish) "Choose Avatar:" else "अवतार चुनें:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = CandyOrange
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        avatars.forEach { av ->
                            val isChosen = av == selectedAvatar
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isChosen) CandyOrange else Color(0xFFFFF3E0))
                                    .clickable {
                                        selectedAvatar = av
                                        profileSavedBanner = ""
                                        SoundEffectHelper.playPopSound()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = av, fontSize = 18.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // EDITABLE CHILD NAME FIELD
                    OutlinedTextField(
                        value = childNameInput,
                        onValueChange = {
                            childNameInput = it
                            profileSavedBanner = ""
                        },
                        label = { Text(if (userProfile.isEnglish) "Student Name" else "बच्चे का नाम (Student Name)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // AGE SELECTOR
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (userProfile.isEnglish) "Age:" else "उम्र (Age):",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) Color.White else Color(0xFF37474F)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            ageOptions.take(6).forEach { age ->
                                val isSelected = age == selectedAge
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) CandyOrange else Color(0xFFECEFF1))
                                        .clickable {
                                            selectedAge = age
                                            profileSavedBanner = ""
                                            SoundEffectHelper.playPopSound()
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$age",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isSelected) Color.White else Color(0xFF455A64)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // SAVE PROFILE BUTTON
                    ClayButton(
                        onClick = {
                            val cleanName = childNameInput.trim().ifEmpty { if (userProfile.isEnglish) "Alex" else "आरव" }
                            onUpdateProfile(cleanName, selectedAvatar, selectedAge)
                            profileSavedBanner = if (userProfile.isEnglish) "✅ $cleanName's profile saved successfully!" else "✅ $cleanName की प्रोफाइल सफलतापूर्वक सेव हो गई!"
                            SoundEffectHelper.playCorrectChime()
                            if (userProfile.isEnglish) {
                                ttsManager?.speak("Great job $cleanName! Your profile is saved!")
                            } else {
                                ttsManager?.speak("शाबाश $cleanName! आपकी प्रोफाइल सुरक्षित कर दी गई है!")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        primaryColor = CorrectGreen,
                        shadowColor = Color(0xFF27AE60),
                        topHighlightColor = Color(0xFF58D68D)
                    ) {
                        Text(
                            text = if (userProfile.isEnglish) "💾 Save Profile" else "💾 नाम और प्रोफाइल सेव करें",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4. LOGIN / LOGOUT SECTION
                    if (userProfile.isLoggedIn) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFE8F5E9))
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (userProfile.isEnglish) "✅ Cloud Account Connected" else "✅ सुरक्षित क्लाउड खाता सक्रिय",
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = if (userProfile.isEnglish) "Parent Contact: ${userProfile.parentEmailOrPhone}" else "माता-पिता संपर्क: ${userProfile.parentEmailOrPhone}",
                                color = Color(0xFF388E3C),
                                fontSize = 12.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            ClayButton(
                                onClick = {
                                    onLogoutParent()
                                    parentContactInput = ""
                                    SoundEffectHelper.playPopSound()
                                    if (userProfile.isEnglish) {
                                        ttsManager?.speak("You have been logged out successfully.")
                                    } else {
                                        ttsManager?.speak("आप सफलतापूर्वक लॉगआउट हो गए हैं।")
                                    }
                                },
                                primaryColor = Color(0xFFFF5252),
                                shadowColor = Color(0xFFD32F2F),
                                topHighlightColor = Color(0xFFFF8A80)
                            ) {
                                Text(
                                    text = if (userProfile.isEnglish) "🚪 Log Out" else "🚪 लॉगआउट करें (Log Out)",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    } else {
                        ClayButton(
                            onClick = {
                                generateNewGateQuestion()
                                showLoginDialog = true
                            },
                            primaryColor = Color(0xFF00B0FF),
                            shadowColor = Color(0xFF0081CB),
                            topHighlightColor = Color(0xFF80D8FF)
                        ) {
                            Text(
                                text = if (userProfile.isEnglish) "🔐 Parent Login" else "🔐 माता-पिता लॉगिन (Parent Login)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }

        // 4.5. Student Comprehensive Automatic Progress Report
        item {
            ClayCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp,
                elevation = 6.dp,
                bgGradient = if (isDarkMode) {
                    Brush.verticalGradient(listOf(Color(0xFF262A56), Color(0xFF1E2142)))
                } else {
                    Brush.verticalGradient(listOf(Color(0xFFFFF9E6), Color(0xFFFFF3E0)))
                },
                borderHighlightColor = CandyOrange
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (userProfile.isEnglish) "📊 Student Progress Report" else "📊 विद्यार्थी प्रगति रिपोर्ट (Report)",
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            color = if (isDarkMode) Color.White else Color(0xFF263238)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(CandyOrange)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (userProfile.isEnglish) "Rank #${userProfile.currentRank} 🏆" else "रैंक #${userProfile.currentRank} 🏆",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Overall completion
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (userProfile.isEnglish) "Overall Progress: ${userProfile.overallProgressPercent}% Complete" else "कुल शिक्षण प्रगति: ${userProfile.overallProgressPercent}% पूरी",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) Color(0xFFA5B0E0) else Color(0xFF455A64)
                        )
                        Text(
                            text = "${userProfile.totalXp} XP • ${userProfile.totalStars} ⭐",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = CandyOrange
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { (userProfile.overallProgressPercent / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = CorrectGreen,
                        trackColor = if (isDarkMode) Color(0xFF1A1D36) else Color(0xFFECEFF1)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Subject-wise 3 Progress Bars
                    SubjectProgressRow(
                        subjectName = if (userProfile.isEnglish) "Hindi Letters" else "हिंदी वर्णमाला",
                        emoji = "🔤",
                        percent = userProfile.hindiProgressPercent,
                        unlockedLevel = userProfile.hindiUnlockedLevel,
                        barColor = CandyOrange,
                        isDarkMode = isDarkMode
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    SubjectProgressRow(
                        subjectName = if (userProfile.isEnglish) "English Phonics" else "अंग्रेजी ABC",
                        emoji = "🅰️",
                        percent = userProfile.englishProgressPercent,
                        unlockedLevel = userProfile.englishUnlockedLevel,
                        barColor = Color(0xFF00B0FF),
                        isDarkMode = isDarkMode
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    SubjectProgressRow(
                        subjectName = if (userProfile.isEnglish) "Math & Numbers" else "गणित 123",
                        emoji = "🔢",
                        percent = userProfile.mathProgressPercent,
                        unlockedLevel = userProfile.mathUnlockedLevel,
                        barColor = Color(0xFF00E676),
                        isDarkMode = isDarkMode
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Activity counters (4 metrics in chips)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ReportMetricChip(label = if (userProfile.isEnglish) "Cards" else "कार्ड्स", count = "${userProfile.cardsLearnedCount}", emoji = "🎴", isDarkMode = isDarkMode)
                        ReportMetricChip(label = if (userProfile.isEnglish) "Tracing" else "स्लेट अक्षर", count = "${userProfile.lettersTracedCount}", emoji = "✍️", isDarkMode = isDarkMode)
                        ReportMetricChip(label = if (userProfile.isEnglish) "Words" else "शब्द", count = "${userProfile.wordsBuiltCount}", emoji = "🧩", isDarkMode = isDarkMode)
                        ReportMetricChip(label = if (userProfile.isEnglish) "Quiz" else "क्विज", count = "${userProfile.quizzesSolvedCount}", emoji = "🎯", isDarkMode = isDarkMode)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }

        // 5. Settings Items
        item {
            Text(
                text = if (userProfile.isEnglish) "⚙️ App Settings:" else "⚙️ ऐप सेटिंग्स (App Settings):",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = if (isDarkMode) Color.White else Color(0xFF37474F)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Language Selection Card (Bilingual: Hindi / English)
        item {
            ClayCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                cornerRadius = 18.dp,
                elevation = 3.dp,
                bgGradient = if (isDarkMode) Brush.verticalGradient(listOf(Color(0xFF1E2142), Color(0xFF262A56)))
                else Brush.verticalGradient(listOf(Color.White, Color(0xFFFFF9E6))),
                borderHighlightColor = CandyOrange
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🌐", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (userProfile.isEnglish) "App Language / भाषा" else "ऐप की भाषा (App Language)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (isDarkMode) Color.White else Color(0xFF263238)
                            )
                            Text(
                                text = if (userProfile.isEnglish) "Choose Hindi or English" else "हिंदी या अंग्रेज़ी भाषा चुनें",
                                fontSize = 11.sp,
                                color = if (isDarkMode) Color(0xFFA5B0E0) else Color(0xFF78909C)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Hindi button
                        val isHi = !userProfile.isEnglish
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isHi) Brush.horizontalGradient(listOf(Color(0xFFFF9800), Color(0xFFFF5722)))
                                    else Brush.horizontalGradient(listOf(Color(0xFFEEEEEE), Color(0xFFE0E0E0)))
                                )
                                .clickable {
                                    if (userProfile.isEnglish) {
                                        SoundEffectHelper.playPopSound()
                                        onToggleLanguage("hi")
                                    }
                                }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🇮🇳 हिंदी (Hindi)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isHi) Color.White else Color(0xFF455A64)
                            )
                        }

                        // English button
                        val isEn = userProfile.isEnglish
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isEn) Brush.horizontalGradient(listOf(Color(0xFF00B0FF), Color(0xFF2979FF)))
                                    else Brush.horizontalGradient(listOf(Color(0xFFEEEEEE), Color(0xFFE0E0E0)))
                                )
                                .clickable {
                                    if (!userProfile.isEnglish) {
                                        SoundEffectHelper.playPopSound()
                                        onToggleLanguage("en")
                                    }
                                }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🇬🇧 English",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isEn) Color.White else Color(0xFF455A64)
                            )
                        }
                    }
                }
            }
        }

        // Dark Mode Toggle
        item {
            ClayCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                cornerRadius = 18.dp,
                elevation = 3.dp,
                bgGradient = if (isDarkMode) Brush.verticalGradient(listOf(Color(0xFF1E2142), Color(0xFF262A56)))
                else Brush.verticalGradient(listOf(Color.White, Color(0xFFFFF9E6)))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🌙", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (userProfile.isEnglish) "Dark Mode (Night Reading)" else "डार्क मोड (रात में पढ़ने के लिए)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (isDarkMode) Color.White else Color(0xFF263238)
                            )
                            Text(
                                text = if (userProfile.isEnglish) "Gentle on young eyes" else "आंखों पर जोर नहीं पड़ता",
                                fontSize = 11.sp,
                                color = if (isDarkMode) Color(0xFFA5B0E0) else Color(0xFF78909C)
                            )
                        }
                    }

                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { onToggleDarkMode(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CandyOrange,
                            checkedTrackColor = CandyYellow
                        )
                    )
                }
            }
        }

        // Voice Guidance info
        item {
            ClayCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                cornerRadius = 18.dp,
                elevation = 3.dp,
                bgGradient = if (isDarkMode) Brush.verticalGradient(listOf(Color(0xFF1E2142), Color(0xFF262A56)))
                else Brush.verticalGradient(listOf(Color.White, Color(0xFFFFF9E6)))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🗣️", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (userProfile.isEnglish) "Priya Teacher Voice (TTS)" else "हिंदी आवाज मार्गदर्शन",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (isDarkMode) Color.White else Color(0xFF263238)
                            )
                            Text(
                                text = if (userProfile.isEnglish) "Speaking in English & Hindi" else "प्रिया दीदी की मीठी आवाज सक्रिय",
                                fontSize = 11.sp,
                                color = if (isDarkMode) Color(0xFFA5B0E0) else Color(0xFF78909C)
                            )
                        }
                    }

                    Text(text = if (userProfile.isEnglish) "Active ✅" else "चालू है ✅", color = CorrectGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // Reset Progress with Working Confirmation
        item {
            ClayCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                cornerRadius = 18.dp,
                elevation = 3.dp,
                bgGradient = if (isDarkMode) Brush.verticalGradient(listOf(Color(0xFF1E2142), Color(0xFF262A56)))
                else Brush.verticalGradient(listOf(Color.White, Color(0xFFFFF9E6)))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🔄", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (userProfile.isEnglish) "Reset Progress" else "प्रोग्रेस रीसेट करें",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (isDarkMode) Color.White else Color(0xFF263238)
                            )
                            Text(
                                text = if (userProfile.isEnglish) "Start over (XP: ${userProfile.totalXp}, Stars: ${userProfile.totalStars})" else "शुरुआत से दोबारा सीखें (XP: ${userProfile.totalXp}, Stars: ${userProfile.totalStars})",
                                fontSize = 11.sp,
                                color = if (isDarkMode) Color(0xFFA5B0E0) else Color(0xFF78909C)
                            )
                        }
                    }

                    TextButton(onClick = { showResetDialog = true }) {
                        Text(text = if (userProfile.isEnglish) "Reset" else "रीसेट", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Parental Login & Verification Dialog (Dynamic Questions, NO Examples)
    if (showLoginDialog) {
        AlertDialog(
            onDismissRequest = { showLoginDialog = false },
            title = {
                Text(
                    text = if (userProfile.isEnglish) "👨‍👩‍👧 Parent Verification & Login" else "👨‍👩‍👧 माता-पिता सत्यापन व लॉगिन",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = if (userProfile.isEnglish) "Enter mobile number or email to keep your progress safely synced:" else "प्रोग्रेस सुरक्षित रखने के लिए मोबाइल नंबर या ईमेल दर्ज करें:",
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = parentContactInput,
                        onValueChange = { parentContactInput = it },
                        label = { Text(if (userProfile.isEnglish) "Mobile number or Email" else "मोबाइल नंबर या ईमेल") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Parental Gate: Dynamic Math question without examples
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (userProfile.isEnglish) "Parent Gate: $gateNum1 + $gateNum2 = ?" else "वयस्क सत्यापन: $gateNum1 + $gateNum2 = कितना?",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = CandyOrange
                        )

                        TextButton(onClick = { generateNewGateQuestion() }) {
                            Text(text = if (userProfile.isEnglish) "🔄 New Question" else "🔄 नया सवाल", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = parentGateAnswer,
                        onValueChange = { parentGateAnswer = it },
                        label = { Text(if (userProfile.isEnglish) "Enter Answer" else "उत्तर लिखें") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (loginError.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = loginError, color = Color.Red, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val expected = (gateNum1 + gateNum2).toString()
                        if (parentGateAnswer.trim() == expected && parentContactInput.isNotBlank()) {
                            onLoginParent(parentContactInput.trim())
                            showLoginDialog = false
                            loginError = ""
                            SoundEffectHelper.playCorrectChime()
                            if (userProfile.isEnglish) {
                                ttsManager?.speak("Parent verified successfully! Login complete!")
                            } else {
                                ttsManager?.speak("सत्यापन सफल! लॉगिन पूरा हुआ!")
                            }
                        } else {
                            loginError = if (userProfile.isEnglish) "Please enter correct answer and contact!" else "कृपया सही उत्तर और सम्पर्क नंबर भरें!"
                            generateNewGateQuestion()
                        }
                    }
                ) {
                    Text(text = if (userProfile.isEnglish) "Verify & Log In ✅" else "वेरिफाई और लॉगिन करें ✅", fontWeight = FontWeight.Bold, color = CorrectGreen)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLoginDialog = false }) {
                    Text(text = if (userProfile.isEnglish) "Cancel" else "रद्द करें")
                }
            }
        )
    }

    // Working Progress Reset Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(
                    text = if (userProfile.isEnglish) "⚠️ Reset All Progress?" else "⚠️ प्रोग्रेस रीसेट करें?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFFFF5252)
                )
            },
            text = {
                Text(
                    text = if (userProfile.isEnglish) "Are you sure you want to reset all learning progress? This will reset all XP and stars to 0 so you can start learning fresh from the beginning." else "क्या आप सच में सारी प्रोग्रेस रीसेट करना चाहते हैं? इससे आपके सभी XP और सितारे शून्य (0) हो जाएंगे और आप शुरुआत से नए सिरे से सीख सकेंगे।",
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onResetProgress()
                        showResetDialog = false
                        resetSuccessBanner = if (userProfile.isEnglish) "✅ Progress reset successfully! You can start learning from the beginning." else "✅ प्रोग्रेस सफलता से रीसेट हो गई! अब आप दोबारा नए सिरे से सीख सकते हैं।"
                        profileSavedBanner = ""
                        SoundEffectHelper.playPopSound()
                        if (userProfile.isEnglish) {
                            ttsManager?.speak("Progress has been reset! Let's start fresh and learn!")
                        } else {
                            ttsManager?.speak("प्रोग्रेस रीसेट कर दी गई है! नए सिरे से खेलें और सीखें!")
                        }
                    }
                ) {
                    Text(text = if (userProfile.isEnglish) "Yes, Reset All" else "हाँ, सब रीसेट करें", fontWeight = FontWeight.Bold, color = Color(0xFFFF5252))
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(text = if (userProfile.isEnglish) "Cancel" else "रद्द करें")
                }
            }
        )
    }
}

@Composable
fun SubjectProgressRow(
    subjectName: String,
    emoji: String,
    percent: Int,
    unlockedLevel: Int,
    barColor: Color,
    isDarkMode: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = emoji, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = subjectName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) Color.White else Color(0xFF263238)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "• लेवल $unlockedLevel",
                    fontSize = 11.sp,
                    color = if (isDarkMode) Color(0xFFA5B0E0) else Color(0xFF78909C)
                )
            }

            Text(
                text = "$percent%",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = barColor
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = { (percent / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = barColor,
            trackColor = if (isDarkMode) Color(0xFF1E2142) else Color(0xFFEEEEEE)
        )
    }
}

@Composable
fun ReportMetricChip(
    label: String,
    count: String,
    emoji: String,
    isDarkMode: Boolean
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isDarkMode) Color(0xFF1E2142) else Color(0xFFF5F5F5))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "$emoji $count", fontWeight = FontWeight.Black, fontSize = 13.sp, color = if (isDarkMode) Color.White else Color(0xFF263238))
            Text(text = label, fontSize = 10.sp, color = if (isDarkMode) Color(0xFFA5B0E0) else Color(0xFF78909C))
        }
    }
}
