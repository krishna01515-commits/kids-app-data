package com.kidslearning.chutki.ui.screens.games

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.offset
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
fun NumberTrainGame(
    isDarkMode: Boolean,
    isEnglish: Boolean = false,
    ttsManager: HindiTtsManager?,
    userPrefs: UserPreferencesManager? = null,
    onBack: () -> Unit,
    onReward: (xp: Int, stars: Int) -> Unit
) {
    val (savedLevel, savedScore) = remember {
        userPrefs?.getGameProgress("NUMBER_TRAIN") ?: Pair(1, 0)
    }
    var gameLevel by remember { mutableIntStateOf(savedLevel.coerceAtLeast(1)) }
    var roundScore by remember { mutableIntStateOf(savedScore.coerceAtLeast(0)) }
    var currentStreak by remember { mutableIntStateOf(0) }
    var roundCount by remember { mutableIntStateOf(savedScore.coerceAtLeast(0) + 1) }

    var currentRound by remember {
        mutableStateOf(ProceduralGameEngine.generateNumberTrainRound(gameLevel))
    }

    // Persist game level and score automatically
    LaunchedEffect(gameLevel, roundScore) {
        userPrefs?.saveGameProgress("NUMBER_TRAIN", gameLevel, roundScore, roundCount)
    }

    var selectedBogieNumber by remember { mutableStateOf<Int?>(null) }
    var isTrainMoving by remember { mutableStateOf(false) }
    var showConfetti by remember { mutableStateOf(false) }
    var showMilestone by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }

    val trainOffset = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    fun loadNextRound() {
        selectedBogieNumber = null
        isTrainMoving = false
        showConfetti = false
        isProcessing = false

        scope.launch { trainOffset.snapTo(0f) }

        currentRound = ProceduralGameEngine.generateNumberTrainRound(gameLevel)
        roundCount++

        ttsManager?.stop()
        val prompt = if (isEnglish) {
            "Here comes the number train! Put the missing number in the wagon!"
        } else {
            "छुक-छुक रेलगाड़ी आ गई! खाली डिब्बे में सही नंबर लगाओ!"
        }
        ttsManager?.speak(prompt)
    }

    LaunchedEffect(Unit) {
        ttsManager?.stop()
        val prompt = if (isEnglish) {
            "Here comes the number train! Put the missing number in the wagon!"
        } else {
            "छुक-छुक रेलगाड़ी आ गई! खाली डिब्बे में सही नंबर लगाओ!"
        }
        ttsManager?.speak(prompt)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (isDarkMode) Brush.verticalGradient(listOf(Color(0xFF13142B), Color(0xFF1E2142)))
                else Brush.verticalGradient(listOf(Color(0xFFFFF9E6), Color(0xFFE1F5FE)))
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
                        text = if (isEnglish) "🚂 Number Sequence Train" else "🚂 नंबर रेलगाड़ी",
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
                            val prompt = if (isEnglish) "Which number goes into the wagon [ ? ]?" else "खाली डिब्बे में कौन सा नंबर आएगा?"
                            ttsManager?.speak(prompt)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🔊", fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Train Display on Track with Smooth Animation
            ClayCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                cornerRadius = 24.dp,
                elevation = 6.dp,
                bgGradient = Brush.verticalGradient(
                    if (isDarkMode) listOf(Color(0xFF262A56), Color(0xFF1E2142))
                    else listOf(Color.White, Color(0xFFE8F5E9))
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Train Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(x = trainOffset.value.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Engine
                        Box(
                            modifier = Modifier
                                .size(56.dp, 60.dp)
                                .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 8.dp, topEnd = 6.dp, bottomEnd = 6.dp))
                                .background(Color(0xFFE53935)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🚂", fontSize = 32.sp)
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Bogies
                        currentRound.numbers.forEachIndexed { idx, num ->
                            val isBlankSlot = idx == currentRound.missingIndex
                            val displayNum = if (isBlankSlot) selectedBogieNumber else num

                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 3.dp)
                                    .size(52.dp, 56.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isBlankSlot) {
                                            if (displayNum == currentRound.correctNumber) CorrectGreen
                                            else Color(0xFFFFB300)
                                        } else Color(0xFF1E88E5)
                                    )
                                    .border(
                                        2.dp,
                                        if (isBlankSlot) CandyOrange else Color(0xFF0D47A1),
                                        RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = displayNum?.toString() ?: "?",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Railway Track representation
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(Color(0xFF795548))
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isTrainMoving) (if (isEnglish) "🎉 Choo-choo! The train is moving!" else "🎉 छुक-छुक! रेलगाड़ी चल पड़ी!")
                        else (if (isEnglish) "Which number goes in wagon [ ? ]?" else "खाली डिब्बे [ ? ] में क्या आएगा?"),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isTrainMoving) CorrectGreen else CandyOrange
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Number Options
            Text(
                text = if (isEnglish) "Pick the correct number for the sequence:" else "सही नंबर चुनकर डिब्बा पूरा करो:",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) Color(0xFFA5B0E0) else Color(0xFF37474F)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                currentRound.options.forEach { opt ->
                    val isThisCorrect = opt == currentRound.correctNumber
                    ClayButton(
                        onClick = {
                            if (!isProcessing) {
                                selectedBogieNumber = opt
                                if (isThisCorrect) {
                                    isProcessing = true
                                    isTrainMoving = true
                                    showConfetti = true
                                    roundScore++
                                    currentStreak++

                                    SoundEffectHelper.playTrainWhistle()
                                    SoundEffectHelper.playCorrectChime()
                                    SoundEffectHelper.playComboSound(currentStreak)
                                    ttsManager?.stop()

                                    if (isEnglish) {
                                        ttsManager?.speak("Awesome! $opt is correct! The train goes choo-choo!")
                                    } else {
                                        ttsManager?.speak("कमाल! $opt बिल्कुल सही है! रेलगाड़ी चल पड़ी!")
                                    }
                                    onReward(35, 2)

                                    scope.launch {
                                        trainOffset.animateTo(
                                            targetValue = 120f,
                                            animationSpec = tween(1100, easing = FastOutSlowInEasing)
                                        )
                                        delay(400)
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
                                        ttsManager?.speak("Not $opt! Follow the number order!")
                                    } else {
                                        ttsManager?.speak("यह $opt नहीं होगा! क्रम से गिनो: 1, 2, 3...")
                                    }
                                }
                            }
                        },
                        modifier = Modifier.size(68.dp),
                        primaryColor = if (selectedBogieNumber == opt && isThisCorrect) CorrectGreen else CandyOrange,
                        shadowColor = Color(0xFFD84315),
                        topHighlightColor = Color(0xFFFFB74D)
                    ) {
                        Text(
                            text = "$opt",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
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
                primaryColor = Color(0xFF1E88E5),
                shadowColor = Color(0xFF0D47A1),
                topHighlightColor = Color(0xFF64B5F6)
            ) {
                Text(
                    text = if (isEnglish) "Next Train ▶" else "अगली ट्रेन ▶",
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
