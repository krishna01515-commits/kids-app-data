package com.kidslearning.chutki.ui.screens.games

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidslearning.chutki.audio.HindiTtsManager
import com.kidslearning.chutki.audio.SoundEffectHelper
import com.kidslearning.chutki.data.ProceduralGameEngine
import com.kidslearning.chutki.data.UserPreferencesManager
import com.kidslearning.chutki.theme.CandyOrange
import com.kidslearning.chutki.theme.CorrectGreen
import com.kidslearning.chutki.ui.components.ClayButton
import com.kidslearning.chutki.ui.components.ClayCard
import com.kidslearning.chutki.ui.components.LevelMilestoneCelebration
import com.kidslearning.chutki.ui.components.StarConfettiOverlay
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MissingLettersGame(
    isDarkMode: Boolean,
    isEnglish: Boolean = false,
    ttsManager: HindiTtsManager?,
    userPrefs: UserPreferencesManager? = null,
    onBack: () -> Unit,
    onReward: (xp: Int, stars: Int) -> Unit
) {
    val (savedLevel, savedScore) = remember {
        userPrefs?.getGameProgress("MISSING_LETTERS") ?: Pair(1, 0)
    }
    var gameLevel by remember { mutableIntStateOf(savedLevel.coerceAtLeast(1)) }
    var roundScore by remember { mutableIntStateOf(savedScore.coerceAtLeast(0)) }
    var currentStreak by remember { mutableIntStateOf(0) }
    var roundCount by remember { mutableIntStateOf(savedScore.coerceAtLeast(0) + 1) }

    val recentBuffer = remember { mutableStateListOf<String>() }

    var currentRound by remember {
        mutableStateOf(ProceduralGameEngine.generateMissingLettersRound(gameLevel, recentBuffer))
    }

    // Persist game level and score automatically
    LaunchedEffect(gameLevel, roundScore) {
        userPrefs?.saveGameProgress("MISSING_LETTERS", gameLevel, roundScore, roundCount)
    }

    var filledLetter by remember { mutableStateOf<String?>(null) }
    var isSolved by remember { mutableStateOf(false) }
    var showConfetti by remember { mutableStateOf(false) }
    var showMilestone by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun loadNextRound() {
        filledLetter = null
        isSolved = false
        showConfetti = false
        isProcessing = false

        if (!recentBuffer.contains(currentRound.fullWordHindi)) {
            recentBuffer.add(currentRound.fullWordHindi)
            if (recentBuffer.size > 4) recentBuffer.removeAt(0)
        }

        currentRound = ProceduralGameEngine.generateMissingLettersRound(gameLevel, recentBuffer)
        roundCount++

        ttsManager?.stop()
        val prompt = if (isEnglish) {
            "Look at the picture! Which letter is missing?"
        } else {
            "तस्वीर देखो! खाली जगह में कौन सा अक्षर आएगा?"
        }
        ttsManager?.speak(prompt)
    }

    LaunchedEffect(Unit) {
        ttsManager?.stop()
        val prompt = if (isEnglish) {
            "Look at the picture! Which letter is missing?"
        } else {
            "तस्वीर देखो! खाली जगह में कौन सा अक्षर आएगा?"
        }
        ttsManager?.speak(prompt)
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
                        .background(if (isDarkMode) Color(0xFF262A56) else Color.White)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "⬅️", fontSize = 18.sp)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isEnglish) "🔠 Missing Letters Puzzle" else "🔠 अक्षर पहेली",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = if (isDarkMode) Color.White else Color(0xFF263238)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isEnglish) "Level $gameLevel • Round $roundCount" else "लेवल $gameLevel • राउंड $roundCount",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CandyOrange
                        )
                        if (currentStreak > 1) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "🔥 x$currentStreak",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFF5722)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(CandyOrange)
                        .clickable {
                            ttsManager?.stop()
                            val prompt = if (isEnglish) "Fill the missing letter!" else "खाली जगह में सही अक्षर भरो!"
                            ttsManager?.speak(prompt)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🔊", fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Picture Display Box
            ClayCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                cornerRadius = 24.dp,
                elevation = 6.dp,
                bgGradient = Brush.verticalGradient(
                    if (isDarkMode) listOf(Color(0xFF262A56), Color(0xFF1E2142))
                    else listOf(Color.White, Color(0xFFFFF9E6))
                )
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = currentRound.emoji, fontSize = 78.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isSolved) "✅ ${currentRound.fullWordHindi}" else (if (isEnglish) "Find the missing letter:" else "खाली अक्षर चुनो:"),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSolved) CorrectGreen else CandyOrange
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Spelling Letter Boxes: [ Before ] [ Blank ] [ After ]
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentRound.beforeChar.isNotEmpty()) {
                    LetterBox(char = currentRound.beforeChar, isFilled = true, isDarkMode = isDarkMode)
                }

                // Blank / Filled Slot
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            if (isSolved) CorrectGreen
                            else if (isDarkMode) Color(0xFF1E2142) else Color(0xFFFFECB3)
                        )
                        .border(
                            2.5.dp,
                            if (isSolved) Color(0xFF1B5E20) else CandyOrange,
                            RoundedCornerShape(18.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = filledLetter ?: "?",
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isSolved) Color.White else CandyOrange
                    )
                }

                if (currentRound.afterChar.isNotEmpty()) {
                    LetterBox(char = currentRound.afterChar, isFilled = true, isDarkMode = isDarkMode)
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // 4. Options
            Text(
                text = if (isEnglish) "Choose the right letter below:" else "नीचे से सही अक्षर चुनो:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) Color(0xFFA5B0E0) else Color(0xFF5D4037)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                currentRound.options.forEach { opt ->
                    ClayButton(
                        onClick = {
                            if (!isProcessing) {
                                if (opt == currentRound.missingChar) {
                                    isProcessing = true
                                    filledLetter = opt
                                    isSolved = true
                                    showConfetti = true
                                    roundScore++
                                    currentStreak++

                                    SoundEffectHelper.playCorrectChime()
                                    SoundEffectHelper.playComboSound(currentStreak)
                                    ttsManager?.stop()

                                    if (isEnglish) {
                                        ttsManager?.speak("Great job! ${currentRound.fullWordEnglish}!")
                                    } else {
                                        ttsManager?.speak("शाबाश! ${currentRound.fullWordHindi}!")
                                    }
                                    onReward(35, 2)

                                    scope.launch {
                                        delay(1400)
                                        if (roundScore % 5 == 0) {
                                            gameLevel++
                                            SoundEffectHelper.playFanfare()
                                            showMilestone = true
                                        } else {
                                            loadNextRound()
                                        }
                                    }
                                } else {
                                    currentStreak = 0
                                    SoundEffectHelper.playSoftTryAgain()
                                    ttsManager?.stop()
                                    if (isEnglish) {
                                        ttsManager?.speak("Not '$opt'! Try another letter!")
                                    } else {
                                        ttsManager?.speak("‘$opt’ नहीं, कोई और अक्षर लगाओ!")
                                    }
                                }
                            }
                        },
                        modifier = Modifier.size(62.dp),
                        primaryColor = Color(0xFFFFB300),
                        shadowColor = Color(0xFFFFA000),
                        topHighlightColor = Color(0xFFFFD54F)
                    ) {
                        Text(text = opt, fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Next button
            ClayButton(
                onClick = {
                    if (!isProcessing) {
                        SoundEffectHelper.playPopSound()
                        loadNextRound()
                    }
                },
                modifier = Modifier.fillMaxWidth(0.7f),
                primaryColor = CandyOrange,
                shadowColor = Color(0xFFD84315),
                topHighlightColor = Color(0xFFFFB74D)
            ) {
                Text(
                    text = if (isEnglish) "Next Word ▶" else "अगला शब्द ▶",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp
                )
            }
        }

        if (showConfetti) {
            StarConfettiOverlay(onAnimationComplete = { showConfetti = false })
        }

        LevelMilestoneCelebration(
            isShowing = showMilestone,
            level = gameLevel,
            streak = currentStreak,
            isEnglish = isEnglish,
            onContinue = {
                showMilestone = false
                loadNextRound()
            }
        )
    }
}

@Composable
private fun LetterBox(char: String, isFilled: Boolean, isDarkMode: Boolean) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (isDarkMode) Color(0xFF262A56) else Color.White)
            .border(2.dp, if (isDarkMode) Color(0xFF3B417A) else Color(0xFFFFE0B2), RoundedCornerShape(18.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = char,
            fontSize = 38.sp,
            fontWeight = FontWeight.Black,
            color = if (isDarkMode) Color.White else Color(0xFF263238)
        )
    }
}
