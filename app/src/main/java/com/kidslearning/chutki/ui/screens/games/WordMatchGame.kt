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
import com.kidslearning.chutki.data.ContentRepository
import com.kidslearning.chutki.data.UserPreferencesManager
import com.kidslearning.chutki.data.WordMatchRoundData
import com.kidslearning.chutki.theme.CandyOrange
import com.kidslearning.chutki.theme.CorrectGreen
import com.kidslearning.chutki.ui.components.ClayButton
import com.kidslearning.chutki.ui.components.ClayCard
import com.kidslearning.chutki.ui.components.LevelMilestoneCelebration
import com.kidslearning.chutki.ui.components.StarConfettiOverlay
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun WordMatchGame(
    isDarkMode: Boolean,
    isEnglish: Boolean = false,
    ttsManager: HindiTtsManager?,
    userPrefs: UserPreferencesManager? = null,
    onBack: () -> Unit,
    onReward: (xp: Int, stars: Int) -> Unit
) {
    val (savedLevel, savedScore) = remember {
        userPrefs?.getGameProgress("WORD_MATCH") ?: Pair(1, 0)
    }
    var gameLevel by remember { mutableIntStateOf(savedLevel.coerceAtLeast(1)) }
    var roundScore by remember { mutableIntStateOf(savedScore.coerceAtLeast(0)) }
    var currentStreak by remember { mutableIntStateOf(0) }
    var roundCount by remember { mutableIntStateOf(savedScore.coerceAtLeast(0) + 1) }

    // Buffer of last 4 used item IDs to avoid immediate repeats
    val recentBuffer = remember { mutableStateListOf<Int>() }

    var currentRound by remember(isEnglish) {
        mutableStateOf(ContentRepository.generateWordMatchRound(gameLevel, recentBuffer, isEnglish))
    }

    // Persist game level and score automatically
    LaunchedEffect(gameLevel, roundScore) {
        userPrefs?.saveGameProgress("WORD_MATCH", gameLevel, roundScore, roundCount)
    }

    var selectedOption by remember { mutableStateOf<String?>(null) }
    var isCorrect by remember { mutableStateOf(false) }
    var showConfetti by remember { mutableStateOf(false) }
    var showMilestone by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var lastClickTimestamp by remember { mutableStateOf(0L) }
    val scope = rememberCoroutineScope()

    fun loadNextRound() {
        selectedOption = null
        isCorrect = false
        showConfetti = false
        isProcessing = false

        // Update buffer of last 4 used items
        if (!recentBuffer.contains(currentRound.targetItem.id)) {
            recentBuffer.add(currentRound.targetItem.id)
            if (recentBuffer.size > 4) recentBuffer.removeAt(0)
        }

        currentRound = ContentRepository.generateWordMatchRound(gameLevel, recentBuffer, isEnglish)
        roundCount++

        // TTS voice prompt with stop safeguard
        ttsManager?.stop()
        ttsManager?.speak(currentRound.hintPrompt)
    }

    LaunchedEffect(currentRound.targetItem.id, isEnglish) {
        ttsManager?.stop()
        ttsManager?.speak(currentRound.hintPrompt)
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
                        text = if (isEnglish) "🍎 Word & Picture Match" else "🍎 चित्र-शब्द मिलान",
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
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
                            val now = System.currentTimeMillis()
                            if (now - lastClickTimestamp > 400L) {
                                lastClickTimestamp = now
                                ttsManager?.stop()
                                ttsManager?.speak(currentRound.hintPrompt)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🔊", fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Picture Display Box
            ClayCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp),
                cornerRadius = 28.dp,
                elevation = 8.dp,
                bgGradient = Brush.verticalGradient(
                    if (isDarkMode) listOf(Color(0xFF262A56), Color(0xFF1E2142))
                    else listOf(Color.White, Color(0xFFFFF9E6))
                ),
                borderHighlightColor = if (isCorrect) CorrectGreen else CandyOrange
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = currentRound.emoji, fontSize = 84.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isCorrect) "⭐ ${currentRound.correctWord}! ⭐" else (if (isEnglish) "What picture is this?" else "यह कौन सा चित्र है?"),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isCorrect) CorrectGreen else (if (isDarkMode) Color.White else Color(0xFF3E2723))
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Target Drop Slot
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(58.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (isCorrect) Color(0xFFE8F5E9) else (if (isDarkMode) Color(0xFF1E2142) else Color(0xFFFFF3E0)))
                    .border(
                        2.dp,
                        if (isCorrect) CorrectGreen else Color(0xFFFFB74D),
                        RoundedCornerShape(18.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = selectedOption ?: if (isEnglish) "👉 Tap the correct word below 👈" else "👉 नीचे से सही शब्द चुनो 👈",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isCorrect) CorrectGreen else Color(0xFFE65100)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 4. Options Row (Dynamic Count & Debounced)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                currentRound.options.forEach { opt ->
                    val isThisSelected = selectedOption == opt
                    ClayCard(
                        modifier = Modifier
                            .weight(1f)
                            .height(62.dp)
                            .clickable(enabled = !isProcessing) {
                                val now = System.currentTimeMillis()
                                if (now - lastClickTimestamp < 500L || isProcessing) return@clickable
                                lastClickTimestamp = now

                                selectedOption = opt
                                if (opt == currentRound.correctWord) {
                                    isProcessing = true
                                    isCorrect = true
                                    showConfetti = true
                                    roundScore++
                                    currentStreak++

                                    SoundEffectHelper.playCorrectChime()
                                    SoundEffectHelper.playComboSound(currentStreak)
                                    ttsManager?.stop()
                                    ttsManager?.celebrateCorrect(isEnglish)
                                    onReward(35, 2)

                                    scope.launch {
                                        // Every 5 correct answers: level up with 2-second celebration
                                        if (roundScore % 5 == 0) {
                                            gameLevel++
                                            SoundEffectHelper.playFanfare()
                                            showMilestone = true
                                            delay(2000)
                                            showMilestone = false
                                        } else {
                                            delay(1200)
                                        }
                                        loadNextRound()
                                    }
                                } else {
                                    currentStreak = 0
                                    SoundEffectHelper.playSoftTryAgain()
                                    ttsManager?.stop()
                                    if (isEnglish) {
                                        ttsManager?.speak("That says '$opt'! Look carefully at the picture!")
                                    } else {
                                        ttsManager?.speak("यह '$opt' है! ध्यान से चित्र देखो और सही शब्द चुनो!")
                                    }
                                }
                            },
                        cornerRadius = 18.dp,
                        elevation = 4.dp,
                        bgGradient = Brush.verticalGradient(
                            if (isThisSelected && isCorrect) listOf(Color(0xFF66BB6A), Color(0xFF43A047))
                            else if (isDarkMode) listOf(Color(0xFF262A56), Color(0xFF1E2142))
                            else listOf(Color(0xFFFFF8E1), Color(0xFFFFECB3))
                        )
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = opt,
                                fontSize = if (opt.length > 7) 14.sp else 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isThisSelected && isCorrect) Color.White else (if (isDarkMode) Color.White else Color(0xFF3E2723))
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 5. Next Button
            ClayButton(
                onClick = {
                    val now = System.currentTimeMillis()
                    if (now - lastClickTimestamp < 500L || isProcessing) return@ClayButton
                    lastClickTimestamp = now
                    SoundEffectHelper.playPopSound()
                    loadNextRound()
                },
                modifier = Modifier.fillMaxWidth(0.7f),
                primaryColor = CandyOrange,
                shadowColor = Color(0xFFD84315),
                topHighlightColor = Color(0xFFFFB74D)
            ) {
                Text(
                    text = if (isEnglish) "Next Picture ▶" else "अगला चित्र ▶",
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
