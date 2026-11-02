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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import com.kidslearning.chutki.theme.CandyOrange
import com.kidslearning.chutki.theme.CorrectGreen
import com.kidslearning.chutki.ui.components.ClayButton
import com.kidslearning.chutki.ui.components.ClayCard
import com.kidslearning.chutki.ui.components.LevelMilestoneCelebration
import com.kidslearning.chutki.ui.components.StarConfettiOverlay
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OddOneOutGame(
    isDarkMode: Boolean,
    isEnglish: Boolean = false,
    ttsManager: HindiTtsManager?,
    onBack: () -> Unit,
    onReward: (xp: Int, stars: Int) -> Unit
) {
    var gameLevel by remember { mutableIntStateOf(1) }
    var roundScore by remember { mutableIntStateOf(0) }
    var currentStreak by remember { mutableIntStateOf(0) }
    var roundCount by remember { mutableIntStateOf(1) }

    val recentOddIds = remember { mutableStateListOf<String>() }

    var currentRound by remember {
        mutableStateOf(ProceduralGameEngine.generateOddOneOutRound(gameLevel, recentOddIds))
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var isSolved by remember { mutableStateOf(false) }
    var showConfetti by remember { mutableStateOf(false) }
    var showMilestone by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun loadNextRound() {
        selectedIndex = null
        isSolved = false
        showConfetti = false
        isProcessing = false

        val oddItem = currentRound.items[currentRound.oddIndex]
        if (!recentOddIds.contains(oddItem.id)) {
            recentOddIds.add(oddItem.id)
            if (recentOddIds.size > 4) recentOddIds.removeAt(0)
        }

        currentRound = ProceduralGameEngine.generateOddOneOutRound(gameLevel, recentOddIds)
        roundCount++

        ttsManager?.stop()
        val prompt = if (isEnglish) currentRound.promptEnglish else currentRound.promptHindi
        ttsManager?.speak(prompt)
    }

    LaunchedEffect(Unit) {
        ttsManager?.stop()
        val prompt = if (isEnglish) currentRound.promptEnglish else currentRound.promptHindi
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
                        text = if (isEnglish) "🔍 Odd One Out" else "🔍 अलग पहचानो",
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
                            val prompt = if (isEnglish) currentRound.promptEnglish else currentRound.promptHindi
                            ttsManager?.speak(prompt)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🔊", fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Question Banner
            ClayCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 22.dp,
                elevation = 6.dp,
                bgGradient = Brush.horizontalGradient(
                    if (isDarkMode) listOf(Color(0xFF262A56), Color(0xFF1E2142))
                    else listOf(Color.White, Color(0xFFFFF9E6))
                ),
                borderHighlightColor = if (isSolved) CorrectGreen else CandyOrange
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isSolved) {
                            "✅ " + (if (isEnglish) currentRound.explanationEnglish else currentRound.explanationHindi)
                        } else {
                            if (isEnglish) currentRound.promptEnglish else currentRound.promptHindi
                        },
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = if (isSolved) CorrectGreen else (if (isDarkMode) Color.White else Color(0xFF3E2723))
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. 4 Big Choice Cards (2x2 Grid, Debounced)
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(currentRound.items.size) { idx ->
                    val item = currentRound.items[idx]
                    val isChosen = selectedIndex == idx
                    val isThisOdd = idx == currentRound.oddIndex

                    ClayCard(
                        modifier = Modifier
                            .height(130.dp)
                            .clickable(enabled = !isProcessing) {
                                if (!isProcessing) {
                                    selectedIndex = idx
                                    if (isThisOdd) {
                                        isProcessing = true
                                        isSolved = true
                                        showConfetti = true
                                        roundScore++
                                        currentStreak++

                                        SoundEffectHelper.playCorrectChime()
                                        SoundEffectHelper.playComboSound(currentStreak)
                                        ttsManager?.stop()

                                        val praise = if (isEnglish) {
                                            "Super! ${currentRound.explanationEnglish}"
                                        } else {
                                            "शाबाश! बिल्कुल सही! ${currentRound.explanationHindi}"
                                        }
                                        ttsManager?.speak(praise)
                                        onReward(30, 2)

                                        scope.launch {
                                            delay(1500)
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
                                            ttsManager?.speak("These belong together! Find the one that is different!")
                                        } else {
                                            ttsManager?.speak("यह तो मिलता-जुलता है! जो सबसे अलग है उसे खोजो!")
                                        }
                                    }
                                }
                            },
                        cornerRadius = 24.dp,
                        elevation = 6.dp,
                        bgGradient = Brush.verticalGradient(
                            if (isChosen && isSolved) listOf(Color(0xFF66BB6A), Color(0xFF43A047))
                            else if (isDarkMode) listOf(Color(0xFF262A56), Color(0xFF1E2142))
                            else listOf(Color.White, Color(0xFFFFF8E1))
                        ),
                        borderHighlightColor = if (isChosen && isSolved) CorrectGreen else CandyOrange
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = item.emoji, fontSize = 54.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isEnglish) item.nameEnglish else item.nameHindi,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isChosen && isSolved) Color.White else (if (isDarkMode) Color.White else Color(0xFF263238))
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

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
