package com.kidslearning.chutki.ui.screens.games

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.graphics.graphicsLayer
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
fun CardFlipMemoryGame(
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

    var cards by remember {
        mutableStateOf(ProceduralGameEngine.generateCardMemoryCards(gameLevel))
    }

    val matchedIds = remember { mutableStateListOf<Int>() }
    var firstFlippedIndex by remember { mutableStateOf<Int?>(null) }
    var secondFlippedIndex by remember { mutableStateOf<Int?>(null) }
    var isChecking by remember { mutableStateOf(false) }
    var showConfetti by remember { mutableStateOf(false) }
    var showMilestone by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun loadNextRound() {
        matchedIds.clear()
        firstFlippedIndex = null
        secondFlippedIndex = null
        isChecking = false
        showConfetti = false

        cards = ProceduralGameEngine.generateCardMemoryCards(gameLevel)
        roundCount++

        ttsManager?.stop()
        val prompt = if (isEnglish) {
            "Flip the cards and find the matching pairs!"
        } else {
            "उल्टे कार्ड्स में से एक जैसे दो कार्ड्स खोजो!"
        }
        ttsManager?.speak(prompt)
    }

    LaunchedEffect(Unit) {
        ttsManager?.stop()
        val prompt = if (isEnglish) {
            "Flip the cards and find the matching pairs!"
        } else {
            "उल्टे कार्ड्स में से एक जैसे दो कार्ड्स खोजो!"
        }
        ttsManager?.speak(prompt)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (isDarkMode) Brush.verticalGradient(listOf(Color(0xFF13142B), Color(0xFF1E2142)))
                else Brush.verticalGradient(listOf(Color(0xFFEDE7F6), Color(0xFFFFF3E0)))
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
                        text = if (isEnglish) "🃏 Flip Cards Memory" else "🃏 मेमोरी कार्ड मैच",
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
                            val prompt = if (isEnglish) "Find matching pairs!" else "एक जैसे दो कार्ड्स खोजो!"
                            ttsManager?.speak(prompt)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🔊", fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Score Banner
            ClayCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp,
                elevation = 4.dp,
                bgGradient = Brush.horizontalGradient(
                    if (isDarkMode) listOf(Color(0xFF262A56), Color(0xFF1E2142))
                    else listOf(Color(0xFFFFF9E6), Color(0xFFFFE0B2))
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEnglish) "Pairs matched: ${matchedIds.size / 2}/${cards.size / 2}" else "जोड़ियां मिलीं: ${matchedIds.size / 2}/${cards.size / 2}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (isDarkMode) Color.White else Color(0xFF3E2723)
                    )

                    Text(
                        text = if (matchedIds.size == cards.size) (if (isEnglish) "🎉 All Matched!" else "🎉 शानदार! सब मिल गए!") else "⭐ +45 XP",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = if (matchedIds.size == cards.size) CorrectGreen else CandyOrange
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Card Grid (Dynamic Columns & Debounced Flips)
            val gridCols = if (cards.size > 6) 4 else if (cards.size == 4) 2 else 3

            LazyVerticalGrid(
                columns = GridCells.Fixed(gridCols),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(cards.size) { idx ->
                    val card = cards[idx]
                    val isFlipped = idx == firstFlippedIndex || idx == secondFlippedIndex || matchedIds.contains(card.id)
                    val isMatched = matchedIds.contains(card.id)

                    val rotation by animateFloatAsState(
                        targetValue = if (isFlipped) 180f else 0f,
                        animationSpec = tween(durationMillis = 400),
                        label = "card_flip_$idx"
                    )

                    Box(
                        modifier = Modifier
                            .height(if (cards.size > 6) 100.dp else 125.dp)
                            .graphicsLayer {
                                rotationY = rotation
                                cameraDistance = 12f * density
                            }
                            .clickable(enabled = !isChecking && !isFlipped) {
                                SoundEffectHelper.playPopSound()

                                if (firstFlippedIndex == null) {
                                    firstFlippedIndex = idx
                                } else if (secondFlippedIndex == null && idx != firstFlippedIndex) {
                                    secondFlippedIndex = idx
                                    isChecking = true

                                    val firstCard = cards[firstFlippedIndex!!]
                                    val secondCard = cards[secondFlippedIndex!!]

                                    if (firstCard.pairKey == secondCard.pairKey) {
                                        matchedIds.add(firstCard.id)
                                        matchedIds.add(secondCard.id)
                                        currentStreak++

                                        SoundEffectHelper.playCorrectChime()
                                        SoundEffectHelper.playComboSound(currentStreak)
                                        ttsManager?.stop()

                                        val pairName = if (isEnglish) firstCard.labelEnglish else firstCard.labelHindi
                                        if (isEnglish) {
                                            ttsManager?.speak("Awesome! Found both $pairName!")
                                        } else {
                                            ttsManager?.speak("अरे वाह! दोनों $pairName मिल गए!")
                                        }

                                        firstFlippedIndex = null
                                        secondFlippedIndex = null
                                        isChecking = false

                                        if (matchedIds.size == cards.size) {
                                            showConfetti = true
                                            roundScore++
                                            SoundEffectHelper.playFanfare()
                                            ttsManager?.celebrateCorrect(isEnglish)
                                            onReward(45, 3)

                                            scope.launch {
                                                delay(1600)
                                                if (roundScore % 3 == 0) {
                                                    gameLevel++
                                                    showMilestone = true
                                                } else {
                                                    loadNextRound()
                                                }
                                            }
                                        }
                                    } else {
                                        currentStreak = 0
                                        SoundEffectHelper.playSoftTryAgain()
                                        scope.launch {
                                            delay(850)
                                            firstFlippedIndex = null
                                            secondFlippedIndex = null
                                            isChecking = false
                                        }
                                    }
                                }
                            }
                    ) {
                        if (rotation <= 90f) {
                            // Back of card
                            ClayCard(
                                modifier = Modifier.fillMaxSize(),
                                cornerRadius = 18.dp,
                                elevation = 4.dp,
                                bgGradient = Brush.verticalGradient(
                                    listOf(Color(0xFF7E57C2), Color(0xFF512DA8))
                                )
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(text = "❓", fontSize = 32.sp)
                                }
                            }
                        } else {
                            // Front of card (revealed)
                            ClayCard(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer { rotationY = 180f },
                                cornerRadius = 18.dp,
                                elevation = 4.dp,
                                bgGradient = Brush.verticalGradient(
                                    if (isMatched) listOf(Color(0xFFE8F8F5), Color(0xFFC8E6C9))
                                    else listOf(Color.White, Color(0xFFFFF9E6))
                                ),
                                borderHighlightColor = if (isMatched) CorrectGreen else CandyOrange
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(text = card.emoji, fontSize = if (cards.size > 6) 36.sp else 42.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (isEnglish) card.labelEnglish else card.labelHindi,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isMatched) CorrectGreen else Color(0xFF3E2723)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // New round button
            ClayButton(
                onClick = {
                    if (!isChecking) {
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
                    text = if (isEnglish) "New Round 🔄" else "नया राउंड खेलो 🔄",
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
