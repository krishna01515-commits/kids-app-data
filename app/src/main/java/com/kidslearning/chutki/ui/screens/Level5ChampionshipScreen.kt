package com.kidslearning.chutki.ui.screens

import android.graphics.Paint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidslearning.chutki.audio.HindiTtsManager
import com.kidslearning.chutki.audio.SoundEffectHelper
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class ChampionshipRound {
    ROUND_1_QUIZ,
    ROUND_2_FREE_SLATE,
    ROUND_3_PUZZLE,
    GRAND_CELEBRATION
}

private data class ChampionshipQuizQuestion(
    val questionHindi: String,
    val questionEnglish: String,
    val options: List<String>,
    val correctIndex: Int,
    val praiseHindi: String
)

private data class ChampionshipPuzzleData(
    val part1: String,
    val missingPlaceholder: String,
    val part2: String,
    val options: List<String>,
    val correctOption: String,
    val completedWord: String,
    val illustrationEmoji: String,
    val promptHindi: String,
    val promptEnglish: String
)

@Composable
fun Level5ChampionshipScreen(
    subject: SubjectType,
    userProfile: UserProfile,
    userPrefs: UserPreferencesManager? = null,
    ttsManager: HindiTtsManager? = null,
    onBack: () -> Unit = {},
    onCelebrationDone: () -> Unit = {}
) {
    val isDarkMode = userProfile.isDarkMode
    val isEnglish = userProfile.isEnglish
    var currentRound by remember { mutableStateOf(ChampionshipRound.ROUND_1_QUIZ) }

    // --- Round 1 State ---
    val quizQuestions = remember(subject.id) { getChampionshipQuizQuestions(subject.id) }
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var isOptionCorrect by remember { mutableStateOf<Boolean?>(null) }

    // --- Round 2 State (Free Slate) ---
    val targetSlateChar = remember(subject.id) {
        when (subject) {
            SubjectType.HINDI -> "अ"
            SubjectType.ENGLISH -> "A"
            SubjectType.MATH -> "5"
        }
    }
    val userStrokes = remember { mutableStateListOf<TracingStroke>() }
    var currentPoints = remember { mutableStateListOf<Offset>() }
    var selectedChalkColor by remember { mutableStateOf(Color(0xFFFFEB3B)) }
    var slateFeedback by remember { mutableStateOf<String?>(null) }

    // --- Round 3 State (Puzzle) ---
    val puzzleData = remember(subject.id) { getChampionshipPuzzle(subject.id) }
    var chosenPuzzleOption by remember { mutableStateOf<String?>(null) }
    var isPuzzleSolved by remember { mutableStateOf(false) }

    // --- Celebration State ---
    var showConfetti by remember { mutableStateOf(false) }
    var showParentCertificateDialog by remember { mutableStateOf(false) }

    // Voice announcement when round changes
    LaunchedEffect(currentRound) {
        when (currentRound) {
            ChampionshipRound.ROUND_1_QUIZ -> {
                userPrefs?.setLastChampionshipProgress(
                    subjectId = subject.id,
                    roundNumber = 1,
                    roundLabel = "राउंड 1: क्विक क्विज़"
                )
                if (isEnglish) {
                    ttsManager?.speak("Welcome to Level 5 Grand Championship! Round 1: Quick Quiz! Answer 5 fast questions to prove your mastery!")
                } else {
                    ttsManager?.speak("लेवल 5 महा मुकाबला में आपका स्वागत है! राउंड 1: क्विक क्विज! 5 तेज सवालों के सही जवाब दो और आगे बढ़ो!")
                }
            }
            ChampionshipRound.ROUND_2_FREE_SLATE -> {
                userPrefs?.setLastChampionshipProgress(
                    subjectId = subject.id,
                    roundNumber = 2,
                    roundLabel = "राउंड 2: फ्री स्लेट"
                )
                SoundEffectHelper.playFanfare()
                if (isEnglish) {
                    ttsManager?.speak("Awesome! Round 2: Free Magic Slate! Draw '$targetSlateChar' with your finger on the slate without any dots!")
                } else {
                    ttsManager?.speak("शाबाश! राउंड 2: फ्री स्लेट चैलेंज! बिना किसी डॉट के जादुई स्लेट पर अपनी उंगली से '$targetSlateChar' लिखकर दिखाओ!")
                }
            }
            ChampionshipRound.ROUND_3_PUZZLE -> {
                userPrefs?.setLastChampionshipProgress(
                    subjectId = subject.id,
                    roundNumber = 3,
                    roundLabel = "राउंड 3: मास्टर पहेली"
                )
                SoundEffectHelper.playFanfare()
                if (isEnglish) {
                    ttsManager?.speak("Round 3: Master Puzzle! Tap the correct card to solve the final mystery!")
                } else {
                    ttsManager?.speak("राउंड 3: मास्टर पहेली! सही कार्ड चुनकर इस आखरी पहेली को सुलझाओ और ट्रॉफी जीतो!")
                }
            }
            ChampionshipRound.GRAND_CELEBRATION -> {
                showConfetti = true
                SoundEffectHelper.playChampionshipFanfare()
                userPrefs?.recordChampionshipWon(subject.id)
                val subjName = if (isEnglish) subject.titleEnglish else subject.titleHindi
                ttsManager?.celebrateChampionshipWin(userProfile.name, subjName, isEnglish)
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
                    onClick = onBack,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isDarkMode) Color(0xFF262A56) else Color(0xFFFFECB3))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = if (isDarkMode) Color.White else Color(0xFF37474F)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isEnglish) "Level 5: Grand Championship 🏆" else "लेवल 5: महा मुकाबला 🏆",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = if (isDarkMode) Color.White else Color(0xFF263238)
                    )
                    Text(
                        text = when (currentRound) {
                            ChampionshipRound.ROUND_1_QUIZ -> if (isEnglish) "Round 1 of 3: Quick Quiz 🎯" else "राउंड 1/3: क्विक क्विज 🎯"
                            ChampionshipRound.ROUND_2_FREE_SLATE -> if (isEnglish) "Round 2 of 3: Free Slate ✍️" else "राउंड 2/3: फ्री स्लेट ✍️"
                            ChampionshipRound.ROUND_3_PUZZLE -> if (isEnglish) "Round 3 of 3: Master Puzzle 🧩" else "राउंड 3/3: मास्टर पहेली 🧩"
                            ChampionshipRound.GRAND_CELEBRATION -> if (isEnglish) "Grand Victory Celebration! 🌟" else "महा मुकाबला विजेता! 🌟"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CandyOrange
                    )
                }

                // Kid Profile Avatar Badge
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

            Spacer(modifier = Modifier.height(10.dp))

            // --- 2. Round Progress Badges ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RoundBadge(
                    stepNumber = "1",
                    title = if (isEnglish) "Quiz" else "क्विज",
                    icon = "🎯",
                    isActive = currentRound == ChampionshipRound.ROUND_1_QUIZ,
                    isDone = currentRound.ordinal > ChampionshipRound.ROUND_1_QUIZ.ordinal,
                    isDarkMode = isDarkMode
                )
                ProgressDash(isDone = currentRound.ordinal > ChampionshipRound.ROUND_1_QUIZ.ordinal)
                RoundBadge(
                    stepNumber = "2",
                    title = if (isEnglish) "Slate" else "स्लेट",
                    icon = "✍️",
                    isActive = currentRound == ChampionshipRound.ROUND_2_FREE_SLATE,
                    isDone = currentRound.ordinal > ChampionshipRound.ROUND_2_FREE_SLATE.ordinal,
                    isDarkMode = isDarkMode
                )
                ProgressDash(isDone = currentRound.ordinal > ChampionshipRound.ROUND_2_FREE_SLATE.ordinal)
                RoundBadge(
                    stepNumber = "3",
                    title = if (isEnglish) "Puzzle" else "पहेली",
                    icon = "🧩",
                    isActive = currentRound == ChampionshipRound.ROUND_3_PUZZLE,
                    isDone = currentRound.ordinal > ChampionshipRound.ROUND_3_PUZZLE.ordinal,
                    isDarkMode = isDarkMode
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // --- 3. Content for Active Round ---
            when (currentRound) {
                ChampionshipRound.ROUND_1_QUIZ -> {
                    Round1QuickQuizView(
                        question = quizQuestions[currentQuestionIndex],
                        questionIndex = currentQuestionIndex,
                        totalQuestions = quizQuestions.size,
                        selectedOptionIndex = selectedOptionIndex,
                        isOptionCorrect = isOptionCorrect,
                        isDarkMode = isDarkMode,
                        isEnglish = isEnglish,
                        onOptionSelected = { optionIdx ->
                            if (selectedOptionIndex != null) return@Round1QuickQuizView
                            selectedOptionIndex = optionIdx
                            val question = quizQuestions[currentQuestionIndex]
                            val isCorrect = optionIdx == question.correctIndex
                            isOptionCorrect = isCorrect

                            if (isCorrect) {
                                SoundEffectHelper.playCoinSound()
                                ttsManager?.speak(question.praiseHindi)
                            } else {
                                SoundEffectHelper.playWrongBoing()
                                ttsManager?.speak(if (isEnglish) "Nice try! Next question!" else "कोई बात नहीं! अगला सवाल देखते हैं!")
                            }
                        },
                        onNext = {
                            selectedOptionIndex = null
                            isOptionCorrect = null
                            if (currentQuestionIndex < quizQuestions.size - 1) {
                                currentQuestionIndex++
                            } else {
                                // Round 1 completed! Transition to Round 2
                                currentRound = ChampionshipRound.ROUND_2_FREE_SLATE
                            }
                        }
                    )
                }

                ChampionshipRound.ROUND_2_FREE_SLATE -> {
                    Round2FreeSlateView(
                        targetChar = targetSlateChar,
                        userStrokes = userStrokes,
                        currentPoints = currentPoints,
                        selectedColor = selectedChalkColor,
                        onColorSelected = { selectedChalkColor = it },
                        feedback = slateFeedback,
                        isDarkMode = isDarkMode,
                        isEnglish = isEnglish,
                        onClear = {
                            userStrokes.clear()
                            currentPoints.clear()
                            slateFeedback = null
                            SoundEffectHelper.playPopSound()
                        },
                        onSubmit = {
                            val totalPoints = userStrokes.sumOf { it.points.size }
                            if (totalPoints >= 6) {
                                slateFeedback = "✅ शाबाश! बहुत ही सुंदर लिखा है! 🌟"
                                SoundEffectHelper.playCorrectChime()
                                if (isEnglish) {
                                    ttsManager?.speak("Outstanding writing! You conquered Round 2!")
                                } else {
                                    ttsManager?.speak("अरे वाह! बहुत ही सुंदर लिखा है! राउंड 2 भी पूरा हुआ!")
                                }
                                currentRound = ChampionshipRound.ROUND_3_PUZZLE
                            } else {
                                slateFeedback = "⚠️ पहले स्लेट पर अपनी उंगली से लिखो!"
                                SoundEffectHelper.playSoftTryAgain()
                                if (isEnglish) {
                                    ttsManager?.speak("Write on the slate with your finger first!")
                                } else {
                                    ttsManager?.speak("नन्हे उस्ताद, पहले स्लेट पर अपनी उंगली से लिखकर तो दिखाओ!")
                                }
                            }
                        }
                    )
                }

                ChampionshipRound.ROUND_3_PUZZLE -> {
                    Round3PuzzleView(
                        puzzle = puzzleData,
                        chosenOption = chosenPuzzleOption,
                        isSolved = isPuzzleSolved,
                        isDarkMode = isDarkMode,
                        isEnglish = isEnglish,
                        onOptionTapped = { opt ->
                            chosenPuzzleOption = opt
                            if (opt == puzzleData.correctOption) {
                                isPuzzleSolved = true
                                SoundEffectHelper.playCoinSound()
                                SoundEffectHelper.playStarTwinkle()
                                if (isEnglish) {
                                    ttsManager?.speak("Bravo! The puzzle is solved! You won the Championship!")
                                } else {
                                    ttsManager?.speak("वाह! पहेली हल हो गई! आप महा मुकाबला जीत गए!")
                                }
                                currentRound = ChampionshipRound.GRAND_CELEBRATION
                            } else {
                                SoundEffectHelper.playSoftTryAgain()
                                if (isEnglish) {
                                    ttsManager?.speak("Try another option!")
                                } else {
                                    ttsManager?.speak("यह सही नहीं लगा, दूसरा विकल्प चुनो!")
                                }
                            }
                        }
                    )
                }

                ChampionshipRound.GRAND_CELEBRATION -> {
                    GrandCelebrationView(
                        subject = subject,
                        userProfile = userProfile,
                        isDarkMode = isDarkMode,
                        isEnglish = isEnglish,
                        onSaveAndDone = {
                            SoundEffectHelper.playPopSound()
                            onCelebrationDone()
                        },
                        onPlayAgain = {
                            // Reset championship
                            currentQuestionIndex = 0
                            selectedOptionIndex = null
                            isOptionCorrect = null
                            userStrokes.clear()
                            currentPoints.clear()
                            slateFeedback = null
                            chosenPuzzleOption = null
                            isPuzzleSolved = false
                            currentRound = ChampionshipRound.ROUND_1_QUIZ
                        },
                        onShowParentCertificate = {
                            showParentCertificateDialog = true
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Confetti Particle Explosion
        StarConfettiOverlay(
            trigger = showConfetti,
            onFinished = { showConfetti = false }
        )

        // Parent Certificate Dialog
        if (showParentCertificateDialog) {
            ParentCertificateDialog(
                subject = subject,
                userProfile = userProfile,
                onDismiss = { showParentCertificateDialog = false }
            )
        }
    }
}

// -------------------------------------------------------------
// ROUND 1: QUICK QUIZ VIEW (5 FAST QUESTIONS)
// -------------------------------------------------------------
@Composable
private fun Round1QuickQuizView(
    question: ChampionshipQuizQuestion,
    questionIndex: Int,
    totalQuestions: Int,
    selectedOptionIndex: Int?,
    isOptionCorrect: Boolean?,
    isDarkMode: Boolean,
    isEnglish: Boolean,
    onOptionSelected: (Int) -> Unit,
    onNext: () -> Unit
) {
    ClayCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 24.dp,
        bgGradient = Brush.verticalGradient(
            if (isDarkMode) listOf(Color(0xFF1E2142), Color(0xFF262A56))
            else listOf(Color.White, Color(0xFFFFF9E6))
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Question Counter & Progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "सवाल ${questionIndex + 1} / $totalQuestions",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = CandyOrange
                )
                Text(
                    text = "⚡ क्विक स्पीड टेस्ट",
                    fontSize = 12.sp,
                    color = Color(0xFF90A4AE),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { (questionIndex + 1).toFloat() / totalQuestions },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = CandyOrange,
                trackColor = if (isDarkMode) Color(0xFF151833) else Color(0xFFE0E0E0)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Teacher encouragement row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TeacherAvatar3D(
                    size = 72.dp,
                    pose = if (isOptionCorrect == true) TeacherPose.CHEERING_STARS else TeacherPose.POINTING_AT_CARD,
                    isSpeaking = false
                )

                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isDarkMode) Color(0xFF282C58) else Color(0xFFFFF3E0))
                        .padding(12.dp)
                ) {
                    Text(
                        text = if (isEnglish) question.questionEnglish else question.questionHindi,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (isDarkMode) Color.White else Color(0xFF263238),
                        lineHeight = 22.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 4 Options Grid (2x2)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                for (row in 0 until 2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (col in 0 until 2) {
                            val optionIdx = row * 2 + col
                            if (optionIdx < question.options.size) {
                                val optionText = question.options[optionIdx]
                                val isSelected = selectedOptionIndex == optionIdx
                                val isThisCorrect = optionIdx == question.correctIndex

                                val bgColor = when {
                                    isSelected && isOptionCorrect == true -> CorrectGreen
                                    isSelected && isOptionCorrect == false -> Color(0xFFFF5252)
                                    selectedOptionIndex != null && isThisCorrect -> CorrectGreen.copy(alpha = 0.85f)
                                    isDarkMode -> Color(0xFF2B2F5C)
                                    else -> Color(0xFFF1F5F9)
                                }

                                val textColor = if (selectedOptionIndex != null && (isSelected || isThisCorrect)) Color.White
                                else if (isDarkMode) Color.White else Color(0xFF263238)

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(64.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(bgColor)
                                        .border(
                                            width = if (isSelected) 2.5.dp else 1.dp,
                                            color = if (isSelected) Color.White else Color(0x33000000),
                                            shape = RoundedCornerShape(18.dp)
                                        )
                                        .clickable(enabled = selectedOptionIndex == null) {
                                            onOptionSelected(optionIdx)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = optionText,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Next / Proceed Button if answered
            if (selectedOptionIndex != null) {
                ClayButton(
                    onClick = onNext,
                    modifier = Modifier.fillMaxWidth(0.85f),
                    primaryColor = if (isOptionCorrect == true) CorrectGreen else CandyOrange
                ) {
                    Text(
                        text = if (questionIndex < totalQuestions - 1) "अगला सवाल ➡️" else "राउंड 2 पर चलें 🚀",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// ROUND 2: FREE SLATE CHALLENGE (NO GUIDED DOTS)
// -------------------------------------------------------------
@Composable
private fun Round2FreeSlateView(
    targetChar: String,
    userStrokes: MutableList<TracingStroke>,
    currentPoints: MutableList<Offset>,
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    feedback: String?,
    isDarkMode: Boolean,
    isEnglish: Boolean,
    onClear: () -> Unit,
    onSubmit: () -> Unit
) {
    val chalkColors = listOf(
        Color(0xFFFFEB3B), // Yellow
        Color(0xFF00E676), // Green
        Color(0xFF00E5FF), // Cyan
        Color(0xFFFF4081), // Pink
        Color(0xFFFFFFFF)  // White
    )

    ClayCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 24.dp,
        bgGradient = Brush.verticalGradient(
            if (isDarkMode) listOf(Color(0xFF1E2142), Color(0xFF262A56))
            else listOf(Color.White, Color(0xFFFFF9E6))
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Instruction
            Text(
                text = if (isEnglish) "Free Slate Challenge ✍️" else "राउंड 2: फ्री स्लेट चैलेंज ✍️",
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = CandyOrange
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (isEnglish) {
                    "Draw '$targetChar' on the Magic Slate with your finger! No guided dots!"
                } else {
                    "बिना किसी डॉट या गाइड के स्लेट पर अपनी उंगली से '$targetChar' बनाओ!"
                },
                fontSize = 13.sp,
                color = if (isDarkMode) Color.LightGray else Color(0xFF546E7A),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Target Character Reference Card
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(CandyOrange.copy(alpha = 0.15f))
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "लक्ष्य अक्षर: $targetChar",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = CandyOrange
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Magic Slate Blackboard Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF131722))
                    .border(3.dp, Color(0xFF3F51B5), RoundedCornerShape(20.dp))
            ) {
                // Background subtle blackboard texture / hint
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    currentPoints.clear()
                                    currentPoints.add(offset)
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    currentPoints.add(change.position)
                                },
                                onDragEnd = {
                                    if (currentPoints.isNotEmpty()) {
                                        userStrokes.add(
                                            TracingStroke(
                                                points = currentPoints.toList(),
                                                color = selectedColor,
                                                strokeWidth = 32f
                                            )
                                        )
                                        currentPoints.clear()
                                    }
                                }
                            )
                        }
                ) {
                    // Draw user committed strokes
                    userStrokes.forEach { stroke ->
                        if (stroke.points.size > 1) {
                            val path = Path().apply {
                                moveTo(stroke.points.first().x, stroke.points.first().y)
                                for (i in 1 until stroke.points.size) {
                                    lineTo(stroke.points[i].x, stroke.points[i].y)
                                }
                            }
                            drawPath(
                                path = path,
                                color = stroke.color,
                                style = Stroke(
                                    width = stroke.strokeWidth,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }

                    // Draw current dragging stroke
                    if (currentPoints.size > 1) {
                        val path = Path().apply {
                            moveTo(currentPoints.first().x, currentPoints.first().y)
                            for (i in 1 until currentPoints.size) {
                                lineTo(currentPoints[i].x, currentPoints[i].y)
                            }
                        }
                        drawPath(
                            path = path,
                            color = selectedColor,
                            style = Stroke(
                                width = 32f,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }

                // If slate is completely empty, show friendly helper hint
                if (userStrokes.isEmpty() && currentPoints.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "उंगली से यहाँ '$targetChar' लिखो ✨",
                            color = Color(0x55FFFFFF),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Chalk Color Palette & Clear Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    chalkColors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (selectedColor == color) 3.dp else 1.dp,
                                    color = if (selectedColor == color) Color.White else Color.DarkGray,
                                    shape = CircleShape
                                )
                                .clickable { onColorSelected(color) }
                        )
                    }
                }

                IconButton(
                    onClick = onClear,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF5252))
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Clear",
                        tint = Color.White
                    )
                }
            }

            if (feedback != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = feedback,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (feedback.startsWith("✅")) CorrectGreen else Color(0xFFFF5252)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Submit / Check Button
            ClayButton(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(0.85f),
                primaryColor = CorrectGreen
            ) {
                Text(
                    text = "जादू चेक करो ✨ (Submit)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }
    }
}

// -------------------------------------------------------------
// ROUND 3: MASTER PUZZLE VIEW (WORD BLEND / MATH EQUATION)
// -------------------------------------------------------------
@Composable
private fun Round3PuzzleView(
    puzzle: ChampionshipPuzzleData,
    chosenOption: String?,
    isSolved: Boolean,
    isDarkMode: Boolean,
    isEnglish: Boolean,
    onOptionTapped: (String) -> Unit
) {
    ClayCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 24.dp,
        bgGradient = Brush.verticalGradient(
            if (isDarkMode) listOf(Color(0xFF1E2142), Color(0xFF262A56))
            else listOf(Color.White, Color(0xFFFFF9E6))
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isEnglish) "Round 3: Master Puzzle 🧩" else "राउंड 3: मास्टर पहेली 🧩",
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = CandyOrange
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isEnglish) puzzle.promptEnglish else puzzle.promptHindi,
                fontSize = 13.sp,
                color = if (isDarkMode) Color.LightGray else Color(0xFF546E7A),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Puzzle Formula / Blend Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Part 1 Card
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF4A90E2))
                        .shadow(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = puzzle.part1,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "+", fontSize = 28.sp, fontWeight = FontWeight.Black, color = CandyOrange)
                Spacer(modifier = Modifier.width(8.dp))

                // Missing Target Slot (Drop Zone)
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            if (isSolved) CorrectGreen
                            else if (isDarkMode) Color(0xFF2C3246)
                            else Color(0xFFFFECB3)
                        )
                        .border(
                            width = 2.dp,
                            color = if (isSolved) Color.White else CandyOrange,
                            shape = RoundedCornerShape(18.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isSolved) puzzle.correctOption else (chosenOption ?: "?"),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isSolved) Color.White else CandyOrange
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "=", fontSize = 28.sp, fontWeight = FontWeight.Black, color = CandyOrange)
                Spacer(modifier = Modifier.width(8.dp))

                // Result Card
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFFE91E63))
                        .shadow(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = puzzle.illustrationEmoji, fontSize = 20.sp)
                        Text(
                            text = if (isSolved) puzzle.completedWord else "?",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "नीचे दिए गए विकल्पों में से सही कार्ड चुनो:",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDarkMode) Color(0xFFA5B0E0) else Color(0xFF78909C)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Option Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                puzzle.options.forEach { opt ->
                    val isCorrect = opt == puzzle.correctOption
                    val isSelected = chosenOption == opt

                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                when {
                                    isSelected && isCorrect -> CorrectGreen
                                    isSelected && !isCorrect -> Color(0xFFFF5252)
                                    isDarkMode -> Color(0xFF2E335C)
                                    else -> Color(0xFFEDE7F6)
                                }
                            )
                            .border(
                                width = if (isSelected) 2.5.dp else 1.dp,
                                color = if (isSelected) Color.White else Color(0xFF7E57C2),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable(enabled = !isSolved) {
                                onOptionTapped(opt)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = opt,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isSelected) Color.White else if (isDarkMode) Color.White else Color(0xFF311B92)
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// GRAND CELEBRATION VIEW (SPINNING TROPHY & CERTIFICATE CARD)
// -------------------------------------------------------------
@Composable
private fun GrandCelebrationView(
    subject: SubjectType,
    userProfile: UserProfile,
    isDarkMode: Boolean,
    isEnglish: Boolean,
    onSaveAndDone: () -> Unit,
    onPlayAgain: () -> Unit,
    onShowParentCertificate: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "grand_celebration")

    // 3D Trophy spinning angle
    val trophyRotationAngle by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "trophy_wobble"
    )

    // Sunburst ray rotation
    val rayRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sunburst_rays"
    )

    // Sparkle scale pulse
    val sparkleScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparkle_pulse"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- 1. 3D Golden Trophy Centerpiece ---
        Box(
            modifier = Modifier
                .size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            // Sunburst Light Rays Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(rayRotation)
            ) {
                val center = Offset(size.width / 2, size.height / 2)
                val rayCount = 12
                val radius = size.minDimension / 1.8f
                for (i in 0 until rayCount) {
                    val angle = (i * 2 * PI / rayCount).toFloat()
                    val x = center.x + radius * cos(angle)
                    val y = center.y + radius * sin(angle)
                    drawLine(
                        color = Color(0xFFFFD54F).copy(alpha = 0.35f),
                        start = center,
                        end = Offset(x, y),
                        strokeWidth = 14f,
                        cap = StrokeCap.Round
                    )
                }
            }

            // 3D Golden Trophy Vector Model
            Canvas(
                modifier = Modifier
                    .size(150.dp)
                    .graphicsLayer {
                        rotationY = trophyRotationAngle
                        cameraDistance = 16f * density
                    }
            ) {
                val w = size.width
                val h = size.height

                // Base Pedestal
                val baseGradient = Brush.verticalGradient(
                    listOf(Color(0xFF8D6E63), Color(0xFF4E342E))
                )
                drawRoundRect(
                    brush = baseGradient,
                    topLeft = Offset(w * 0.28f, h * 0.82f),
                    size = Size(w * 0.44f, h * 0.16f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
                )

                // Gold Plaque on base
                drawRoundRect(
                    color = Color(0xFFFFD700),
                    topLeft = Offset(w * 0.34f, h * 0.86f),
                    size = Size(w * 0.32f, h * 0.08f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                )

                // Trophy Stem
                val goldGradient = Brush.horizontalGradient(
                    listOf(Color(0xFFFFD700), Color(0xFFFFF176), Color(0xFFFFA000))
                )
                drawRoundRect(
                    brush = goldGradient,
                    topLeft = Offset(w * 0.44f, h * 0.62f),
                    size = Size(w * 0.12f, h * 0.22f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                )

                // Trophy Cup Body
                val cupPath = Path().apply {
                    moveTo(w * 0.22f, h * 0.18f)
                    lineTo(w * 0.78f, h * 0.18f)
                    cubicTo(
                        w * 0.78f, h * 0.48f,
                        w * 0.66f, h * 0.64f,
                        w * 0.50f, h * 0.64f
                    )
                    cubicTo(
                        w * 0.34f, h * 0.64f,
                        w * 0.22f, h * 0.48f,
                        w * 0.22f, h * 0.18f
                    )
                    close()
                }
                drawPath(path = cupPath, brush = goldGradient)

                // Cup Rim
                drawOval(
                    brush = Brush.horizontalGradient(listOf(Color(0xFFFFF59D), Color(0xFFFFB300))),
                    topLeft = Offset(w * 0.22f, h * 0.14f),
                    size = Size(w * 0.56f, h * 0.08f)
                )

                // Left Handle
                val leftHandle = Path().apply {
                    moveTo(w * 0.24f, h * 0.24f)
                    cubicTo(
                        w * 0.08f, h * 0.24f,
                        w * 0.08f, h * 0.50f,
                        w * 0.30f, h * 0.52f
                    )
                }
                drawPath(
                    path = leftHandle,
                    brush = goldGradient,
                    style = Stroke(width = 10f, cap = StrokeCap.Round)
                )

                // Right Handle
                val rightHandle = Path().apply {
                    moveTo(w * 0.76f, h * 0.24f)
                    cubicTo(
                        w * 0.92f, h * 0.24f,
                        w * 0.92f, h * 0.50f,
                        w * 0.70f, h * 0.52f
                    )
                }
                drawPath(
                    path = rightHandle,
                    brush = goldGradient,
                    style = Stroke(width = 10f, cap = StrokeCap.Round)
                )

                // Center Star Emboss
                drawCircle(
                    color = Color(0xFFFFF9C4),
                    radius = w * 0.09f,
                    center = Offset(w * 0.5f, h * 0.38f)
                )
            }

            // Glistening Sparkle Stars
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .scale(sparkleScale)
            ) {
                Text(text = "✨", fontSize = 28.sp)
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .scale(sparkleScale)
            ) {
                Text(text = "🌟", fontSize = 24.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Headline
        Text(
            text = if (isEnglish) "CHAMPIONSHIP MASTER! 🏆" else "महा मुकाबला मास्टर विजेता! 🏆",
            fontWeight = FontWeight.Black,
            fontSize = 22.sp,
            color = Color(0xFFFFB300),
            textAlign = TextAlign.Center
        )

        Text(
            text = if (isEnglish) "You cleared all 3 rounds and won the Master Trophy!" else "तीनों राउंड पार किए और सुनहरी मास्टर ट्रॉफी हासिल की!",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isDarkMode) Color.White else Color(0xFF455A64),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- 2. SUPER KID MASTER CERTIFICATE CARD ---
        MasterCertificateCard(
            subject = subject,
            userProfile = userProfile,
            isDarkMode = isDarkMode,
            isEnglish = isEnglish
        )

        Spacer(modifier = Modifier.height(18.dp))

        // --- 3. Action Buttons ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ClayButton(
                onClick = onShowParentCertificate,
                modifier = Modifier.weight(1f),
                primaryColor = Color(0xFF673AB7)
            ) {
                Text(
                    text = "👨‍👩‍👧 माता-पिता को दिखाएं",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            ClayButton(
                onClick = onPlayAgain,
                modifier = Modifier.weight(1f),
                primaryColor = Color(0xFF00ACC1)
            ) {
                Text(
                    text = "🔄 दोबारा खेलें",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        ClayButton(
            onClick = onSaveAndDone,
            modifier = Modifier.fillMaxWidth(),
            primaryColor = CorrectGreen
        ) {
            Text(
                text = "🏆 ट्रॉफी सुरक्षित करें और आगे बढ़ें",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
    }
}

// -------------------------------------------------------------
// SUPER KID MASTER CERTIFICATE CARD
// -------------------------------------------------------------
@Composable
private fun MasterCertificateCard(
    subject: SubjectType,
    userProfile: UserProfile,
    isDarkMode: Boolean,
    isEnglish: Boolean
) {
    val currentDateStr = remember {
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("hi", "IN"))
        sdf.format(Date())
    }

    ClayCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 24.dp,
        bgGradient = Brush.verticalGradient(
            listOf(Color(0xFF1A1C3B), Color(0xFF13152C))
        ),
        borderHighlightColor = Color(0xFFFFD700)
    ) {
        Column(
            modifier = Modifier
                .padding(18.dp)
                .border(2.dp, Color(0xFFFFD700).copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Academy Header
            Text(
                text = "🎓 CHUTKI KIDS LEARNING ACADEMY",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD54F),
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "सुपर किड मास्टर प्रमाणपत्र",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            Text(
                text = "SUPER KID MASTER CERTIFICATE",
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFA5B0E0),
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Student Avatar Frame
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(Color(0xFFFFD700), Color(0xFFFFA000))))
                    .border(3.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                KidAvatarView(avatarKey = userProfile.avatarEmoji, size = 68.dp)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Student Name
            Text(
                text = userProfile.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFFFEB3B)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Achievement Description
            Text(
                text = "इन्होंने '${if (isEnglish) subject.titleEnglish else subject.titleHindi}' के सभी 5 लेवल्स व महा मुकाबला को सफलतापूर्वक पार करके 'मास्टर ट्रॉफी 🏆' हासिल की है।",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 5 Stars Row
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(5) {
                    Text(text = "⭐", fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Date & Verified Tutor Seal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "दिनांक:", fontSize = 10.sp, color = Color.Gray)
                    Text(text = currentDateStr, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "प्रिया दीदी (Tutor)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFB300))
                    Text(text = "✅ प्रमाणित मास्टर", fontSize = 10.sp, color = CorrectGreen)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// PARENT CERTIFICATE FULLSCREEN DIALOG
// -------------------------------------------------------------
@Composable
private fun ParentCertificateDialog(
    subject: SubjectType,
    userProfile: UserProfile,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = CorrectGreen)
            ) {
                Text(text = "बहुत बढ़िया! बंद करें 👍", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Text(
                text = "👨‍👩‍👧 माता-पिता का गौरव कार्ड",
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = CandyOrange
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "बधाई हो! आपके बच्चे '${userProfile.name}' ने ${subject.titleHindi} का महा मुकाबला पूरा कर लिया है और सुनहरी मास्टर ट्रॉफी हासिल की है!",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                KidAvatarView(avatarKey = userProfile.avatarEmoji, size = 90.dp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "🏆 लेवल 5 मास्टर चैंपियन",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = Color(0xFFFFB300)
                )
            }
        }
    )
}

// -------------------------------------------------------------
// UI HELPER BADGES FOR 3 ROUNDS PROGRESS
// -------------------------------------------------------------
@Composable
private fun RoundBadge(
    stepNumber: String,
    title: String,
    icon: String,
    isActive: Boolean,
    isDone: Boolean,
    isDarkMode: Boolean
) {
    val bg = when {
        isDone -> CorrectGreen
        isActive -> CandyOrange
        isDarkMode -> Color(0xFF262A56)
        else -> Color(0xFFECEFF1)
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(bg)
                .border(
                    width = if (isActive) 2.5.dp else 1.dp,
                    color = if (isActive) Color.White else Color.Transparent,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isDone) {
                Icon(Icons.Default.Check, contentDescription = "Done", tint = Color.White, modifier = Modifier.size(22.dp))
            } else {
                Text(text = icon, fontSize = 20.sp)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
            color = if (isActive) CandyOrange else if (isDarkMode) Color.LightGray else Color(0xFF546E7A)
        )
    }
}

@Composable
private fun ProgressDash(isDone: Boolean) {
    Box(
        modifier = Modifier
            .width(28.dp)
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(if (isDone) CorrectGreen else Color.LightGray)
    )
}

// -------------------------------------------------------------
// DATA PROVIDERS FOR 3 ROUNDS
// -------------------------------------------------------------
private fun getChampionshipQuizQuestions(subjectId: String): List<ChampionshipQuizQuestion> {
    return when (subjectId.lowercase()) {
        "english" -> listOf(
            ChampionshipQuizQuestion(
                questionHindi = "Phonics Sound: What starts with '/b/ /b/'? ⚽",
                questionEnglish = "Phonics Sound: What starts with '/b/ /b/'? ⚽",
                options = listOf("BALL", "CAT", "DUCK", "FISH"),
                correctIndex = 0,
                praiseHindi = "Great job! B is for Ball!"
            ),
            ChampionshipQuizQuestion(
                questionHindi = "Rhyming Fun: What rhymes with 'CAT' 🐱?",
                questionEnglish = "Rhyming Fun: What rhymes with 'CAT' 🐱?",
                options = listOf("BAT", "SUN", "DOG", "PEN"),
                correctIndex = 0,
                praiseHindi = "Awesome! CAT rhymes with BAT!"
            ),
            ChampionshipQuizQuestion(
                questionHindi = "Missing Letter: S + U + ___ = SUN ☀️",
                questionEnglish = "Missing Letter: S + U + ___ = SUN ☀️",
                options = listOf("N", "T", "P", "M"),
                correctIndex = 0,
                praiseHindi = "Brilliant! S-U-N makes SUN!"
            ),
            ChampionshipQuizQuestion(
                questionHindi = "Animal Riddle: Long trunk & big ears! Who am I? 🐘",
                questionEnglish = "Animal Riddle: Long trunk & big ears! Who am I? 🐘",
                options = listOf("Elephant", "Lion", "Monkey", "Tiger"),
                correctIndex = 0,
                praiseHindi = "You got it! It's an Elephant!"
            ),
            ChampionshipQuizQuestion(
                questionHindi = "First Letter: What letter is for 🍎 Apple?",
                questionEnglish = "First Letter: What letter is for 🍎 Apple?",
                options = listOf("A", "B", "C", "D"),
                correctIndex = 0,
                praiseHindi = "Super! A is for Apple!"
            )
        )
        "math" -> listOf(
            ChampionshipQuizQuestion(
                questionHindi = "गिनो और बताओ: कितने गुब्बारे हैं? 🎈🎈🎈",
                questionEnglish = "Count the balloons: 🎈🎈🎈",
                options = listOf("3", "2", "4", "5"),
                correctIndex = 0,
                praiseHindi = "शाबाश! कुल 3 गुब्बारे हैं!"
            ),
            ChampionshipQuizQuestion(
                questionHindi = "जोड़ बताओ: 2 + 3 = ___ ➕",
                questionEnglish = "Addition: 2 + 3 = ___ ➕",
                options = listOf("5", "4", "6", "3"),
                correctIndex = 0,
                praiseHindi = "बहुत अच्छे! 2 में 3 जोड़ने पर 5 बनता है!"
            ),
            ChampionshipQuizQuestion(
                questionHindi = "संख्या रेलगाड़ी: 11, 12, [ ? ], 14 🚂",
                questionEnglish = "Number Train: 11, 12, [ ? ], 14 🚂",
                options = listOf("13", "15", "10", "16"),
                correctIndex = 0,
                praiseHindi = "वाह! 12 के बाद 13 आता है!"
            ),
            ChampionshipQuizQuestion(
                questionHindi = "आकार पहेली: 3 कोने और 3 भुजाएँ (समोसा जैसा) 🔺",
                questionEnglish = "Shape Riddle: 3 corners & 3 sides (like samosa) 🔺",
                options = listOf("त्रिभुज (Triangle)", "वर्ग (Square)", "गोला (Circle)", "तारा (Star)"),
                correctIndex = 0,
                praiseHindi = "सही जवाब! 3 कोनों वाला त्रिभुज!"
            ),
            ChampionshipQuizQuestion(
                questionHindi = "घटाव बताओ: 5 - 2 = ___ ➖",
                questionEnglish = "Subtraction: 5 - 2 = ___ ➖",
                options = listOf("3", "2", "4", "1"),
                correctIndex = 0,
                praiseHindi = "लाजवाब! 5 में से 2 गए तो 3 बचे!"
            )
        )
        else -> listOf( // Hindi default
            ChampionshipQuizQuestion(
                questionHindi = "पहचानो: 'अ' से क्या होता है? 🍎",
                questionEnglish = "Identify: What starts with 'अ'? 🍎",
                options = listOf("अनार", "आम", "इमली", "उल्लू"),
                correctIndex = 0,
                praiseHindi = "शाबाश! अ से अनार!"
            ),
            ChampionshipQuizQuestion(
                questionHindi = "गायब अक्षर भरो: क + [ ? ] = 'कल' 📅",
                questionEnglish = "Missing letter: क + [ ? ] = 'कल' 📅",
                options = listOf("ल", "म", "र", "त"),
                correctIndex = 0,
                praiseHindi = "अरे वाह! क और ल मिलकर बनता है कल!"
            ),
            ChampionshipQuizQuestion(
                questionHindi = "पहेली: 'जल ही मेरा जीवन है, हाथ लगाओ तो डर जाऊँगी!' 🐟",
                questionEnglish = "Riddle: 'Water is my life, touch me and I get scared!' 🐟",
                options = listOf("म से मछली", "ब से बत्तख", "क से कछुआ", "म से मेंढक"),
                correctIndex = 0,
                praiseHindi = "बिल्कुल सही! मछली जल की रानी है!"
            ),
            ChampionshipQuizQuestion(
                questionHindi = "फलों का राजा कौन कहलाता है? 👑",
                questionEnglish = "Who is known as the King of Fruits? 👑",
                options = listOf("आ से आम 🥭", "अ से अनार 🍎", "स से संतरा 🍊", "अ से अंगूर 🍇"),
                correctIndex = 0,
                praiseHindi = "शाबाश! पीला-पीला मीठा आम!"
            ),
            ChampionshipQuizQuestion(
                questionHindi = "शब्द बनाओ: फ + ल = [ ? ] 🍎",
                questionEnglish = "Make a word: फ + ल = [ ? ] 🍎",
                options = listOf("फल", "घर", "नल", "बस"),
                correctIndex = 0,
                praiseHindi = "लाजवाब! फ और ल मिलकर फल बनता है!"
            )
        )
    }
}

private fun getChampionshipPuzzle(subjectId: String): ChampionshipPuzzleData {
    return when (subjectId.lowercase()) {
        "english" -> ChampionshipPuzzleData(
            part1 = "S",
            missingPlaceholder = "UN",
            part2 = "SUN",
            options = listOf("UN", "AT", "IN", "OP"),
            correctOption = "UN",
            completedWord = "SUN",
            illustrationEmoji = "☀️",
            promptHindi = "खाली बॉक्स में सही कार्ड जोड़कर 'SUN ☀️' बनाओ!",
            promptEnglish = "Tap the correct blend to complete 'SUN ☀️'!"
        )
        "math" -> ChampionshipPuzzleData(
            part1 = "3",
            missingPlaceholder = "2",
            part2 = "5",
            options = listOf("2", "1", "4", "3"),
            correctOption = "2",
            completedWord = "5 ⭐",
            illustrationEmoji = "⭐",
            promptHindi = "खाली बॉक्स में सही संख्या चुनो ताकि 3 + ? = 5 हो!",
            promptEnglish = "Choose the right number so 3 + ? = 5!"
        )
        else -> ChampionshipPuzzleData(
            part1 = "घ",
            missingPlaceholder = "र",
            part2 = "घर",
            options = listOf("र", "न", "स", "ल"),
            correctOption = "र",
            completedWord = "घर",
            illustrationEmoji = "🏠",
            promptHindi = "खाली बॉक्स में सही अक्षर जोड़कर 'घर 🏠' शब्द बनाओ!",
            promptEnglish = "Tap the missing letter to complete the word 'Ghar 🏠'!"
        )
    }
}

