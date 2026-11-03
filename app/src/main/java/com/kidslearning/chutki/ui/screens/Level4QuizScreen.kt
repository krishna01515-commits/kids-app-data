package com.kidslearning.chutki.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kidslearning.chutki.audio.HindiTtsManager
import com.kidslearning.chutki.audio.SoundEffectHelper
import com.kidslearning.chutki.data.LearningContentRepository
import com.kidslearning.chutki.data.UserPreferencesManager
import com.kidslearning.chutki.model.QuizQuestion
import com.kidslearning.chutki.model.SubjectType
import com.kidslearning.chutki.theme.CandyOrange
import com.kidslearning.chutki.theme.CandyYellow
import com.kidslearning.chutki.theme.CorrectGreen
import com.kidslearning.chutki.theme.WrongRed
import com.kidslearning.chutki.ui.components.ClayButton
import com.kidslearning.chutki.ui.components.ClayCard
import com.kidslearning.chutki.ui.components.StarConfettiOverlay
import com.kidslearning.chutki.ui.components.TeacherAvatar3D
import com.kidslearning.chutki.ui.components.TeacherPose
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun Level4QuizScreen(
    subject: SubjectType,
    isDarkMode: Boolean,
    isEnglish: Boolean = false,
    ttsManager: HindiTtsManager?,
    userPrefs: UserPreferencesManager? = null,
    onBack: () -> Unit,
    onAddReward: (xp: Int, stars: Int) -> Unit = { _, _ -> },
    onQuizAnswered: (questionId: Int) -> Unit = {},
    onNavigateToNextLevel: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val questions = remember(subject.id) {
        LearningContentRepository.getQuizzes(subject.id)
    }

    val savedIndex = remember(subject.id) {
        userPrefs?.getLastQuizIndex(subject.id) ?: 0
    }

    var questionIndex by remember(subject.id) {
        mutableIntStateOf(savedIndex.coerceIn(0, (questions.size - 1).coerceAtLeast(0)))
    }

    val currentQuestion = questions.getOrNull(questionIndex) ?: questions.first()
    val accentColor = Color(subject.themeColorHex)

    // Option interaction states
    val hiddenOptionIndices = remember { mutableStateListOf<Int>() }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var isCorrectAnswer by remember { mutableStateOf<Boolean?>(null) }
    var hasUsedSecondChance by remember { mutableStateOf(false) }

    // Celebration states
    var showConfetti by remember { mutableStateOf(false) }
    var showStarsCelebration by remember { mutableStateOf(false) }
    var isLevelFinished by remember { mutableStateOf(false) }

    // On question change: Reset option states and persist progress
    LaunchedEffect(questionIndex) {
        hiddenOptionIndices.clear()
        selectedOptionIndex = null
        isCorrectAnswer = null
        hasUsedSecondChance = false
        showConfetti = false
        showStarsCelebration = false

        userPrefs?.setLastQuizIndex(subject.id, questionIndex, currentQuestion.questionHindi)
        ttsManager?.speak(currentQuestion.questionHindi)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (isDarkMode) Brush.verticalGradient(listOf(Color(0xFF13142B), Color(0xFF1E2142)))
                else Brush.verticalGradient(listOf(Color(0xFFFFFDF5), Color(0xFFFFF3E0)))
            )
            .padding(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Bar: Back, Title, Speaker
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(if (isDarkMode) Color(0xFF262A56) else Color(0xFFFFECB3))
                            .clickable { onBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "⬅️", fontSize = 20.sp)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isEnglish) "Level 4: Quiz & Riddles 🧩" else "लेवल 4: क्विज़ और पहेलियाँ 🧩",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isDarkMode) Color.White else Color(0xFF263238)
                        )
                        Text(
                            text = if (isEnglish) {
                                "${subject.titleEnglish} • Question ${questionIndex + 1} of ${questions.size}"
                            } else {
                                "${subject.titleHindi} • सवाल ${questionIndex + 1} / ${questions.size}"
                            },
                            fontSize = 12.sp,
                            color = if (isDarkMode) Color(0xFFA5B0E0) else Color(0xFF78909C)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(if (isDarkMode) Color(0xFF262A56) else Color(0xFFFFECB3))
                            .clickable {
                                SoundEffectHelper.playPopSound()
                                ttsManager?.speak(currentQuestion.questionHindi)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🔊", fontSize = 22.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = { (questionIndex + 1).toFloat() / questions.size.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = accentColor,
                    trackColor = if (isDarkMode) Color(0xFF262A56) else Color(0xFFFFE0B2)
                )

                Spacer(modifier = Modifier.height(14.dp))
            }

            // 2. Priya Didi Guidance Banner
            item {
                ClayCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp,
                    elevation = 4.dp,
                    bgGradient = Brush.horizontalGradient(
                        if (isDarkMode) listOf(Color(0xFF262A56), Color(0xFF1E2142))
                        else listOf(Color(0xFFFFECB3), Color(0xFFFFE082))
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
                            pose = if (isCorrectAnswer == true) TeacherPose.CHEERING_STARS
                            else if (hasUsedSecondChance) TeacherPose.THINKING
                            else TeacherPose.POINTING_AT_CARD,
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
                                text = when {
                                    isCorrectAnswer == true -> currentQuestion.praiseHindi
                                    hasUsedSecondChance -> if (isEnglish) {
                                        "“No worries! One wrong option is gone. Try again, you can do it!”"
                                    } else {
                                        "“कोई बात नहीं! एक गलत विकल्प हटा दिया है। दोबारा सोचो और सही चुनो!”"
                                    }
                                    else -> if (isEnglish) {
                                        "“Read the riddle or clue carefully and tap the right answer!”"
                                    } else {
                                        "“पहेली या सवाल को ध्यान से सुनो और सही विकल्प पर टच करो!”"
                                    }
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDarkMode) Color.White else Color(0xFF3E2723)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // 3. Question & Riddle Main Card
            item {
                ClayCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp,
                    elevation = 8.dp,
                    bgGradient = Brush.verticalGradient(
                        if (isDarkMode) listOf(Color(0xFF232850), Color(0xFF1B1D3D))
                        else listOf(Color.White, Color(0xFFFFF9E6))
                    ),
                    borderHighlightColor = accentColor
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Category pill badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(accentColor.copy(alpha = 0.15f))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = when {
                                    currentQuestion.questionHindi.contains("पहेली") || currentQuestion.questionHindi.contains("Riddle") -> "🧩 पहेली बूझो (Riddle)"
                                    currentQuestion.questionHindi.contains("चित्र") -> "🖼️ चित्र पहचानो (Picture Match)"
                                    currentQuestion.questionHindi.contains("गायब") -> "🔤 गायब अक्षर (Missing Letter)"
                                    currentQuestion.questionHindi.contains("Phonics") -> "🎵 Phonics Sound"
                                    currentQuestion.questionHindi.contains("Rhyming") -> "🔤 Rhyming Words"
                                    currentQuestion.questionHindi.contains("गिनो") -> "🔢 गिनो और बताओ (Count & Tap)"
                                    currentQuestion.questionHindi.contains("रेलगाड़ी") -> "🚂 संख्या रेलगाड़ी (Number Train)"
                                    currentQuestion.questionHindi.contains("आकार") -> "📐 आकार पहेली (Shape Riddle)"
                                    else -> "⭐ मज़ेदार सवाल"
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Prompt Emoji / Visual
                        Text(
                            text = currentQuestion.promptEmoji,
                            fontSize = 48.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Question Text in Hindi / English
                        Text(
                            text = currentQuestion.questionHindi,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            color = if (isDarkMode) Color.White else Color(0xFF263238),
                            lineHeight = 26.sp
                        )

                        // 2nd Chance feedback indicator
                        if (hasUsedSecondChance && isCorrectAnswer == null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFF9800).copy(alpha = 0.2f))
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "✨ दूसरा मौका: एक गलत विकल्प हटा दिया गया है!",
                                    color = Color(0xFFFF9800),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
            }

            // 4. Options Grid (2x2 or 4 rows)
            items(currentQuestion.options.size) { optionIndex ->
                val optionText = currentQuestion.options[optionIndex]
                val isHidden = optionIndex in hiddenOptionIndices
                val isCorrect = optionIndex == currentQuestion.correctIndex
                val isSelected = selectedOptionIndex == optionIndex

                if (!isHidden) {
                    val cardBg = when {
                        isCorrectAnswer == true && isCorrect -> Brush.horizontalGradient(
                            listOf(CorrectGreen, Color(0xFF27AE60))
                        )
                        isSelected && !isCorrect -> Brush.horizontalGradient(
                            listOf(WrongRed, Color(0xFFD63031))
                        )
                        // If 2nd mistake was made, highlight correct answer gently
                        isCorrectAnswer == false && isCorrect -> Brush.horizontalGradient(
                            listOf(CorrectGreen.copy(alpha = 0.9f), Color(0xFF27AE60))
                        )
                        isDarkMode -> Brush.horizontalGradient(
                            listOf(Color(0xFF262A56), Color(0xFF1E2142))
                        )
                        else -> Brush.horizontalGradient(
                            listOf(Color.White, Color(0xFFFFFDF5))
                        )
                    }

                    ClayCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clickable(enabled = isCorrectAnswer != true) {
                                SoundEffectHelper.playPopSound()

                                if (isCorrect) {
                                    // Sahi Jawab!
                                    selectedOptionIndex = optionIndex
                                    isCorrectAnswer = true
                                    SoundEffectHelper.playCoinSound()
                                    showConfetti = true
                                    showStarsCelebration = true
                                    ttsManager?.speak(currentQuestion.praiseHindi)

                                    onAddReward(20, 3)
                                    onQuizAnswered(currentQuestion.id)
                                    userPrefs?.recordQuizAnswered(subject.id, currentQuestion.id)

                                    // If last question, complete level
                                    if (questionIndex >= questions.size - 1) {
                                        userPrefs?.unlockLevel(subject.id, 5)
                                    }
                                } else {
                                    // Galat Jawab
                                    if (!hasUsedSecondChance) {
                                        // 2nd Chance! Hide this option and give another chance
                                        hasUsedSecondChance = true
                                        hiddenOptionIndices.add(optionIndex)
                                        SoundEffectHelper.playSoftTryAgain()
                                        ttsManager?.speak(
                                            if (isEnglish) "Oops! Try again, one option is removed!"
                                            else "अरे, थोड़ा सा गलत हो गया! एक विकल्प हटा दिया, फिर कोशिश करो!"
                                        )
                                    } else {
                                        // 2nd mistake made: Show answer gently without punishing
                                        selectedOptionIndex = optionIndex
                                        isCorrectAnswer = false
                                        SoundEffectHelper.playSoftTryAgain()
                                        ttsManager?.speak(
                                            if (isEnglish) "The answer is ${currentQuestion.options[currentQuestion.correctIndex]}! ${currentQuestion.explanationHindi}"
                                            else "सही जवाब है: ${currentQuestion.options[currentQuestion.correctIndex]}! ${currentQuestion.explanationHindi}"
                                        )
                                    }
                                }
                            },
                        cornerRadius = 18.dp,
                        elevation = if (isSelected || (isCorrectAnswer != null && isCorrect)) 8.dp else 4.dp,
                        bgGradient = cardBg,
                        borderHighlightColor = if (isCorrectAnswer == true && isCorrect) CorrectGreen else accentColor.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isCorrectAnswer == true && isCorrect -> Color.White
                                                isSelected && !isCorrect -> Color.White
                                                else -> accentColor.copy(alpha = 0.2f)
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = ('A'.code + optionIndex).toChar().toString(),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp,
                                        color = when {
                                            isCorrectAnswer == true && isCorrect -> CorrectGreen
                                            isSelected && !isCorrect -> WrongRed
                                            else -> accentColor
                                        }
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Text(
                                    text = optionText,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        (isCorrectAnswer == true && isCorrect) || (isSelected && !isCorrect) || (isCorrectAnswer == false && isCorrect) -> Color.White
                                        isDarkMode -> Color.White
                                        else -> Color(0xFF263238)
                                    }
                                )
                            }

                            // Right icon (check / cross / arrow)
                            if (isCorrectAnswer == true && isCorrect) {
                                Text(text = "✅", fontSize = 20.sp)
                            } else if (isSelected && !isCorrect) {
                                Text(text = "❌", fontSize = 20.sp)
                            }
                        }
                    }
                } else {
                    // Option is hidden for 2nd chance: show friendly empty placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .height(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, Color.Gray.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                            .background(Color.Gray.copy(alpha = 0.06f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🚫 विकल्प हटाया गया (2nd Chance)",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // 5. Navigation Row: Previous & Next / Finish
            item {
                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous Question
                    ClayButton(
                        onClick = {
                            if (questionIndex > 0) {
                                questionIndex--
                            }
                        },
                        modifier = Modifier.weight(1f),
                        primaryColor = if (isDarkMode) Color(0xFF262A56) else Color(0xFFCFD8DC),
                        shadowColor = if (isDarkMode) Color(0xFF1B1D3D) else Color(0xFFB0BEC5),
                        topHighlightColor = if (isDarkMode) Color(0xFF333866) else Color(0xFFECEFF1),
                        enabled = questionIndex > 0
                    ) {
                        Text(
                            text = "◀ पिछला",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (questionIndex > 0) {
                                if (isDarkMode) Color.White else Color(0xFF37474F)
                            } else Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    // Next / Finish Question
                    ClayButton(
                        onClick = {
                            if (questionIndex < questions.size - 1) {
                                questionIndex++
                            } else {
                                isLevelFinished = true
                            }
                        },
                        modifier = Modifier.weight(1f),
                        primaryColor = if (isCorrectAnswer == true) CorrectGreen else CandyOrange,
                        shadowColor = if (isCorrectAnswer == true) Color(0xFF27AE60) else Color(0xFFD95B00),
                        topHighlightColor = if (isCorrectAnswer == true) Color(0xFF81C784) else Color(0xFFFFB049)
                    ) {
                        Text(
                            text = if (questionIndex < questions.size - 1) "अगला सवाल ▶" else "लेवल समाप्त 🏆",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }

        // =========================================================================
        // 6. Confetti Burst
        // =========================================================================
        StarConfettiOverlay(
            trigger = showConfetti,
            onFinished = { showConfetti = false }
        )

        // =========================================================================
        // 7. 3-Stars Pop-up Celebration Dialog (⭐⭐⭐ + Coin Sound + XP)
        // =========================================================================
        if (showStarsCelebration) {
            Dialog(
                onDismissRequest = { showStarsCelebration = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.55f))
                        .clickable { showStarsCelebration = false },
                    contentAlignment = Alignment.Center
                ) {
                    val starScale = remember { Animatable(0.2f) }

                    LaunchedEffect(Unit) {
                        starScale.animateTo(
                            targetValue = 1.0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    }

                    ClayCard(
                        modifier = Modifier
                            .fillMaxWidth(0.88f)
                            .scale(starScale.value),
                        cornerRadius = 28.dp,
                        elevation = 12.dp,
                        bgGradient = Brush.verticalGradient(
                            listOf(Color(0xFFFFF9E6), Color(0xFFFFECB3))
                        ),
                        borderHighlightColor = CandyYellow
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // 3 Animated Stars Pop
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "⭐", fontSize = 42.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "⭐",
                                    fontSize = 54.sp,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "⭐", fontSize = 42.sp)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "शानदार! बिल्कुल सही जवाब!",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF2E7D32),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // XP & Coin Reward Badge
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFFFFD54F))
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "🪙", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "+20 XP",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF5D4037)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "⭐ +3 सितारे",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF5D4037)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = currentQuestion.explanationHindi,
                                fontSize = 13.sp,
                                color = Color(0xFF455A64),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            ClayButton(
                                onClick = {
                                    showStarsCelebration = false
                                    if (questionIndex < questions.size - 1) {
                                        questionIndex++
                                    } else {
                                        isLevelFinished = true
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(0.85f),
                                primaryColor = CorrectGreen,
                                shadowColor = Color(0xFF27AE60)
                            ) {
                                Text(
                                    text = if (questionIndex < questions.size - 1) "अगला सवाल ➔" else "शानदार! समाप्त 🏆",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // 8. Level Finished Trophy Celebration Modal
        // =========================================================================
        if (isLevelFinished) {
            Dialog(
                onDismissRequest = { isLevelFinished = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.65f)),
                    contentAlignment = Alignment.Center
                ) {
                    ClayCard(
                        modifier = Modifier.fillMaxWidth(0.9f),
                        cornerRadius = 28.dp,
                        elevation = 14.dp,
                        bgGradient = Brush.verticalGradient(
                            listOf(Color.White, Color(0xFFFFF8E1))
                        ),
                        borderHighlightColor = CandyYellow
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "🏆", fontSize = 60.sp)

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "बधाई हो! लेवल 4 पूरा हुआ!",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = CandyOrange,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "आपने ${subject.titleHindi} की सभी पहेलियाँ और क्विज़ सफलतापूर्वक हल कर ली हैं!",
                                fontSize = 14.sp,
                                color = Color(0xFF455A64),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Unlocked Level 5 badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(CorrectGreen.copy(alpha = 0.15f))
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "🎉 लेवल 5 (मेगा चैलेंज) अनलॉक हो गया!",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = CorrectGreen
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            ClayButton(
                                onClick = {
                                    isLevelFinished = false
                                    onBack()
                                },
                                modifier = Modifier.fillMaxWidth(0.85f),
                                primaryColor = CandyOrange
                            ) {
                                Text(
                                    text = "लेवल सूची पर जाएँ ⬅️",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

