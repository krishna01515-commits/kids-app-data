package com.kidslearning.chutki.ui.screens.games

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
import androidx.compose.foundation.shape.CircleShape
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
fun ShadowMatchingGame(
    isDarkMode: Boolean,
    isEnglish: Boolean = false,
    ttsManager: HindiTtsManager?,
    userPrefs: UserPreferencesManager? = null,
    onBack: () -> Unit,
    onReward: (xp: Int, stars: Int) -> Unit
) {
    val (savedLevel, savedScore) = remember {
        userPrefs?.getGameProgress("SHADOW_MATCHING") ?: Pair(1, 0)
    }
    var gameLevel by remember { mutableIntStateOf(savedLevel.coerceAtLeast(1)) }
    var roundScore by remember { mutableIntStateOf(savedScore.coerceAtLeast(0)) }
    var currentStreak by remember { mutableIntStateOf(0) }
    var roundCount by remember { mutableIntStateOf(savedScore.coerceAtLeast(0) + 1) }

    val recentIds = remember { mutableStateListOf<String>() }

    var currentRound by remember {
        mutableStateOf(ProceduralGameEngine.generateShadowMatchingRound(gameLevel, recentIds))
    }

    // Persist game level and score automatically
    LaunchedEffect(gameLevel, roundScore) {
        userPrefs?.saveGameProgress("SHADOW_MATCHING", gameLevel, roundScore, roundCount)
    }

    var selectedEmoji by remember { mutableStateOf<String?>(null) }
    var isSolved by remember { mutableStateOf(false) }
    var showConfetti by remember { mutableStateOf(false) }
    var showMilestone by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun loadNextRound() {
        selectedEmoji = null
        isSolved = false
        showConfetti = false
        isProcessing = false

        if (!recentIds.contains(currentRound.targetItem.id)) {
            recentIds.add(currentRound.targetItem.id)
            if (recentIds.size > 4) recentIds.removeAt(0)
        }

        currentRound = ProceduralGameEngine.generateShadowMatchingRound(gameLevel, recentIds)
        roundCount++

        ttsManager?.stop()
        val prompt = if (isEnglish) {
            "Look at the picture! Match with its dark shadow silhouette below!"
        } else {
            "यह तस्वीर देखो! नीचे से इसकी सही परछाई चुनो!"
        }
        ttsManager?.speak(prompt)
    }

    LaunchedEffect(Unit) {
        ttsManager?.stop()
        val prompt = if (isEnglish) {
            "Look at the picture! Match with its dark shadow silhouette below!"
        } else {
            "यह तस्वीर देखो! नीचे से इसकी सही परछाई चुनो!"
        }
        ttsManager?.speak(prompt)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (isDarkMode) Brush.verticalGradient(listOf(Color(0xFF13142B), Color(0xFF1E2142)))
                else Brush.verticalGradient(listOf(Color(0xFFFFFDF5), Color(0xFFEDE7F6)))
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
                        text = if (isEnglish) "👥 Shadow Matching" else "👥 परछाई मिलाओ",
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
                            val prompt = if (isEnglish) "Find the matching shadow silhouette!" else "इसकी सही परछाई पहचानो!"
                            ttsManager?.speak(prompt)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🔊", fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Original Object Box
            ClayCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp),
                cornerRadius = 28.dp,
                elevation = 8.dp,
                bgGradient = Brush.verticalGradient(
                    if (isDarkMode) listOf(Color(0xFF262A56), Color(0xFF1E2142))
                    else listOf(Color.White, Color(0xFFFFF9E6))
                ),
                borderHighlightColor = if (isSolved) CorrectGreen else CandyOrange
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = currentRound.targetItem.emoji, fontSize = 76.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isSolved) {
                            "⭐ " + (if (isEnglish) "Shadow Matched! ${currentRound.targetItem.nameEnglish}!" else "परछाई मिल गई! ${currentRound.targetItem.nameHindi}!") + " ⭐"
                        } else {
                            if (isEnglish) "Which is its correct shadow?" else "इसकी सही परछाई कौन सी है?"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isSolved) CorrectGreen else CandyOrange
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (isEnglish) "Tap the dark shadow silhouette:" else "काली परछाई (Silhouette) पर टैप करो:",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) Color(0xFFA5B0E0) else Color(0xFF455A64)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Shadow Options (Debounced & Dynamic)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                currentRound.options.forEach { opt ->
                    val isChosen = selectedEmoji == opt.emoji
                    val isTarget = opt.id == currentRound.targetItem.id

                    ClayCard(
                        modifier = Modifier
                            .weight(1f)
                            .height(125.dp)
                            .clickable(enabled = !isProcessing) {
                                if (!isProcessing) {
                                    selectedEmoji = opt.emoji
                                    if (isTarget) {
                                        isProcessing = true
                                        isSolved = true
                                        showConfetti = true
                                        roundScore++
                                        currentStreak++

                                        SoundEffectHelper.playCorrectChime()
                                        SoundEffectHelper.playComboSound(currentStreak)
                                        ttsManager?.stop()
                                        ttsManager?.celebrateCorrect(isEnglish)
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
                                            ttsManager?.speak("This shadow has a different shape! Look closely!")
                                        } else {
                                            ttsManager?.speak("यह परछाई अलग आकार की है! ध्यान से मिलाओ!")
                                        }
                                    }
                                }
                            },
                        cornerRadius = 22.dp,
                        elevation = 6.dp,
                        bgGradient = Brush.verticalGradient(
                            if (isChosen && isSolved) listOf(Color(0xFFE8F8F5), Color(0xFFC8E6C9))
                            else if (isDarkMode) listOf(Color(0xFF1E2142), Color(0xFF151833))
                            else listOf(Color(0xFF37474F), Color(0xFF263238))
                        ),
                        borderHighlightColor = if (isChosen && isSolved) CorrectGreen else Color(0xFF78909C)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = opt.emoji,
                                fontSize = 52.sp,
                                modifier = Modifier.then(
                                    if (!isSolved || !isChosen) Modifier.background(Color.Black.copy(alpha = 0.65f), CircleShape).padding(4.dp)
                                    else Modifier
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isChosen && isSolved) (if (isEnglish) "Correct! ✅" else "सही! ✅") else (if (isEnglish) "Shadow ⬛" else "परछाई ⬛"),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isChosen && isSolved) CorrectGreen else Color.LightGray
                            )
                        }
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
                    text = if (isEnglish) "Next Shadow ▶" else "अगली परछाई ▶",
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
