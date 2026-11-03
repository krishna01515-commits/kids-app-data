package com.kidslearning.chutki.ui.screens.games

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import com.kidslearning.chutki.data.CountCollectRoundData
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
fun CountCollectGame(
    isDarkMode: Boolean,
    isEnglish: Boolean = false,
    ttsManager: HindiTtsManager?,
    userPrefs: UserPreferencesManager? = null,
    onBack: () -> Unit,
    onReward: (xp: Int, stars: Int) -> Unit
) {
    val (savedLevel, savedScore) = remember {
        userPrefs?.getGameProgress("COUNT_COLLECT") ?: Pair(1, 0)
    }
    var gameLevel by remember { mutableIntStateOf(savedLevel.coerceAtLeast(1)) }
    var roundScore by remember { mutableIntStateOf(savedScore.coerceAtLeast(0)) }
    var currentStreak by remember { mutableIntStateOf(0) }
    var roundCount by remember { mutableIntStateOf(savedScore.coerceAtLeast(0) + 1) }

    // Buffer of last 4 used item IDs to prevent immediate repeats
    val recentBuffer = remember { mutableStateListOf<Int>() }

    var currentRound by remember {
        mutableStateOf(ContentRepository.generateCountCollectRound(gameLevel, recentBuffer, isEnglish))
    }

    // Persist game level and score automatically
    LaunchedEffect(gameLevel, roundScore) {
        userPrefs?.saveGameProgress("COUNT_COLLECT", gameLevel, roundScore, roundCount)
    }

    var selectedAnswer by remember { mutableStateOf<Int?>(null) }
    var isSolved by remember { mutableStateOf(false) }
    var showConfetti by remember { mutableStateOf(false) }
    var showMilestone by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var lastClickTimestamp by remember { mutableStateOf(0L) }
    val scope = rememberCoroutineScope()

    // Gentle float animation
    val floatAnim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        floatAnim.animateTo(
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1800, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    fun loadNextRound() {
        selectedAnswer = null
        isSolved = false
        showConfetti = false
        isProcessing = false

        // Update buffer with last used item
        if (!recentBuffer.contains(currentRound.targetItem.id)) {
            recentBuffer.add(currentRound.targetItem.id)
            if (recentBuffer.size > 4) recentBuffer.removeAt(0)
        }

        currentRound = ContentRepository.generateCountCollectRound(gameLevel, recentBuffer, isEnglish)
        roundCount++

        // TTS voice prompt with stop safeguard
        ttsManager?.stop()
        val prompt = if (isEnglish) {
            "How many ${currentRound.nameEnglish} are there? Count and pick the right number!"
        } else {
            "स्क्रीन पर कितनी ${currentRound.nameHindi} हैं? गिनो और सही संख्या चुनो!"
        }
        ttsManager?.speak(prompt)
    }

    LaunchedEffect(currentRound.targetItem.id, isEnglish) {
        ttsManager?.stop()
        val prompt = if (isEnglish) {
            "How many ${currentRound.nameEnglish} are there? Count and pick the right number!"
        } else {
            "स्क्रीन पर कितनी ${currentRound.nameHindi} हैं? गिनो और सही संख्या चुनो!"
        }
        ttsManager?.speak(prompt)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (isDarkMode) Brush.verticalGradient(listOf(Color(0xFF13142B), Color(0xFF1E2142)))
                else Brush.verticalGradient(listOf(Color(0xFFE8F5E9), Color(0xFFFFFDE7)))
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
                        text = if (isEnglish) "🔢 Count & Collect" else "🔢 गिनती खेल",
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
                        .background(if (isDarkMode) Color(0xFF262A56) else Color.White)
                        .clickable {
                            val now = System.currentTimeMillis()
                            if (now - lastClickTimestamp > 400L) {
                                lastClickTimestamp = now
                                ttsManager?.stop()
                                val prompt = if (isEnglish) {
                                    "How many ${currentRound.nameEnglish} are there? Count and pick the right number!"
                                } else {
                                    "स्क्रीन पर कितनी ${currentRound.nameHindi} हैं? गिनो और सही संख्या चुनो!"
                                }
                                ttsManager?.speak(prompt)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🔊", fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Playful Count Playground Card
            ClayCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                cornerRadius = 24.dp,
                elevation = 8.dp,
                bgGradient = Brush.verticalGradient(
                    if (isDarkMode) listOf(Color(0xFF1B2A4A), Color(0xFF141E34))
                    else listOf(Color.White, Color(0xFFF1F8E9))
                ),
                borderHighlightColor = if (isSolved) CorrectGreen else Color(0xFF00E676)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    val columns = when {
                        currentRound.count <= 4 -> 2
                        currentRound.count <= 9 -> 3
                        else -> 4
                    }

                    val itemSize = when {
                        currentRound.count <= 4 -> 76.dp
                        currentRound.count <= 9 -> 56.dp
                        else -> 46.dp
                    }

                    val fontSize = when {
                        currentRound.count <= 4 -> 46.sp
                        currentRound.count <= 9 -> 34.sp
                        else -> 28.sp
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(columns),
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        items(currentRound.count) { idx ->
                            val offset = (if (idx % 2 == 0) floatAnim.value else (1f - floatAnim.value)) * 12f - 6f
                            Box(
                                modifier = Modifier
                                    .size(itemSize)
                                    .offset(y = offset.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.35f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = currentRound.emoji, fontSize = fontSize)
                            }
                        }
                    }

                    if (isSolved) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .clip(RoundedCornerShape(14.dp))
                                .background(CorrectGreen)
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (isEnglish) "⭐ Correct! Total ${currentRound.count} ${currentRound.nameEnglish}! ⭐" else "⭐ बिल्कुल सही! कुल ${currentRound.count} ${currentRound.nameHindi}! ⭐",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Number Options (Debounced & Dynamic Distractors)
            Text(
                text = if (isEnglish) "Count and choose the number:" else "गिनो और सही संख्या चुनो:",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) Color(0xFFA5B0E0) else Color(0xFF37474F)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                currentRound.options.forEach { num ->
                    val isChosen = selectedAnswer == num
                    ClayButton(
                        onClick = {
                            val now = System.currentTimeMillis()
                            if (now - lastClickTimestamp < 500L || isProcessing) return@ClayButton
                            lastClickTimestamp = now

                            selectedAnswer = num
                            if (num == currentRound.count) {
                                isProcessing = true
                                isSolved = true
                                showConfetti = true
                                roundScore++
                                currentStreak++

                                SoundEffectHelper.playCorrectChime()
                                SoundEffectHelper.playComboSound(currentStreak)
                                ttsManager?.stop()

                                if (isEnglish) {
                                    ttsManager?.speak("Great job! Exactly $num ${currentRound.nameEnglish}!")
                                } else {
                                    ttsManager?.speak("शाबाश! स्क्रीन पर कुल $num ${currentRound.nameHindi} हैं!")
                                }
                                onReward(30, 2)

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
                                    ttsManager?.speak("That's not $num! Try counting again!")
                                } else {
                                    ttsManager?.speak("यह $num नहीं है! एक बार फिर से गिनो!")
                                }
                            }
                        },
                        modifier = Modifier.size(64.dp),
                        primaryColor = if (isChosen && isSolved) CorrectGreen else Color(0xFF00E676),
                        shadowColor = Color(0xFF00A152),
                        topHighlightColor = Color(0xFF69F0AE)
                    ) {
                        Text(
                            text = "$num",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Next Question button
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
                    text = if (isEnglish) "Next Question ▶" else "अगला सवाल ▶",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp
                )
            }
        }

        if (showConfetti) {
            StarConfettiOverlay(onAnimationComplete = { showConfetti = false })
        }

        // Level Up Milestone Dialog (2-second celebration)
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
