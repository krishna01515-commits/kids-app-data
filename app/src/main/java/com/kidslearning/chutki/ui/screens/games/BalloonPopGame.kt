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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidslearning.chutki.audio.HindiTtsManager
import com.kidslearning.chutki.audio.SoundEffectHelper
import com.kidslearning.chutki.data.BalloonRoundData
import com.kidslearning.chutki.data.ContentRepository
import com.kidslearning.chutki.theme.CandyOrange
import com.kidslearning.chutki.ui.components.ClayButton
import com.kidslearning.chutki.ui.components.ClayCard
import com.kidslearning.chutki.ui.components.LevelMilestoneCelebration
import com.kidslearning.chutki.ui.components.StarConfettiOverlay
import com.kidslearning.chutki.ui.components.TeacherAvatar3D
import com.kidslearning.chutki.ui.components.TeacherPose
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun BalloonPopGame(
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

    // Buffer of last 4 used item IDs to prevent immediate repeats
    val recentBuffer = remember { mutableStateListOf<Int>() }

    var currentRound by remember {
        mutableStateOf(ContentRepository.generateBalloonPopRound(gameLevel, recentBuffer, isEnglish))
    }

    var poppedChar by remember { mutableStateOf<String?>(null) }
    var showConfetti by remember { mutableStateOf(false) }
    var showMilestone by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var lastClickTimestamp by remember { mutableStateOf(0L) }
    val scope = rememberCoroutineScope()

    // Dynamically scale balloon float speed with game level
    val floatDuration = (2200 - (gameLevel * 80).coerceAtMost(900)).coerceAtLeast(1300)
    val floatAnim = remember(gameLevel) { Animatable(0f) }
    LaunchedEffect(gameLevel) {
        floatAnim.animateTo(
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(floatDuration, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    fun loadNextRound() {
        poppedChar = null
        showConfetti = false
        isProcessing = false

        // Update buffer with last used item
        if (!recentBuffer.contains(currentRound.targetItem.id)) {
            recentBuffer.add(currentRound.targetItem.id)
            if (recentBuffer.size > 4) recentBuffer.removeAt(0)
        }

        currentRound = ContentRepository.generateBalloonPopRound(gameLevel, recentBuffer, isEnglish)
        roundCount++

        // TTS voice prompt with stop safeguard
        ttsManager?.stop()
        val promptText = if (isEnglish) {
            "${currentRound.soundPrompt}! ${currentRound.hintEnglish}"
        } else {
            "${currentRound.soundPrompt}! ${currentRound.hintHindi}"
        }
        ttsManager?.speak(promptText)
    }

    LaunchedEffect(Unit) {
        ttsManager?.stop()
        val initialText = if (isEnglish) {
            "${currentRound.soundPrompt}! ${currentRound.hintEnglish}"
        } else {
            "${currentRound.soundPrompt}! ${currentRound.hintHindi}"
        }
        ttsManager?.speak(initialText)
    }

    val balloonColors = listOf(
        Color(0xFFFF5252), // Red
        Color(0xFF448AFF), // Blue
        Color(0xFF69F0AE), // Green
        Color(0xFFFFD740), // Yellow
        Color(0xFFFF4081)  // Pink
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (isDarkMode) Brush.verticalGradient(listOf(Color(0xFF13142B), Color(0xFF1E2142)))
                else Brush.verticalGradient(listOf(Color(0xFFE1F5FE), Color(0xFFFFF9C4)))
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
                        text = if (isEnglish) "🎈 Phonics Balloon Pop" else "🎈 गुब्बारे फोड़ो",
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
                                    "${currentRound.soundPrompt}! ${currentRound.hintEnglish}"
                                } else {
                                    "${currentRound.soundPrompt}! ${currentRound.hintHindi}"
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

            // 2. Teacher Hint Card
            ClayCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp,
                elevation = 6.dp,
                bgGradient = Brush.horizontalGradient(
                    if (isDarkMode) listOf(Color(0xFF21254A), Color(0xFF1E2142))
                    else listOf(Color.White, Color(0xFFFFF8E1))
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
                        pose = if (showConfetti) TeacherPose.CHEERING_STARS else TeacherPose.POINTING_AT_CARD,
                        isSpeaking = false
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = if (isEnglish) "Teacher Priya says:" else "प्रिया दीदी कहती हैं:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = CandyOrange
                        )
                        Text(
                            text = "“${currentRound.soundPrompt}”",
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            color = if (isDarkMode) Color.White else Color(0xFF263238)
                        )
                        Text(
                            text = if (isEnglish) currentRound.hintEnglish else currentRound.hintHindi,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDarkMode) Color(0xFFA5B0E0) else Color(0xFF5D4037)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Floating Balloons Area (Debounced, Procedural from ContentRepository)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    currentRound.options.forEachIndexed { idx, opt ->
                        val isPopped = poppedChar == opt
                        val color = balloonColors[idx % balloonColors.size]
                        val floatOffset = (if (idx % 2 == 0) floatAnim.value else (1f - floatAnim.value)) * 24f - 12f

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .offset(y = floatOffset.dp)
                                .scale(if (isPopped) 1.25f else 1.0f)
                                .clickable(enabled = !isProcessing) {
                                    val now = System.currentTimeMillis()
                                    if (now - lastClickTimestamp < 500L || isProcessing) return@clickable
                                    lastClickTimestamp = now

                                    if (opt == currentRound.targetChar) {
                                        isProcessing = true
                                        poppedChar = opt
                                        showConfetti = true
                                        roundScore++
                                        currentStreak++

                                        SoundEffectHelper.playPopSound()
                                        SoundEffectHelper.playCorrectChime()
                                        SoundEffectHelper.playComboSound(currentStreak)

                                        // Stop TTS before celebration
                                        ttsManager?.stop()
                                        ttsManager?.celebrateCorrect(isEnglish)
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
                                            ttsManager?.speak("That's '$opt'! Look for '${currentRound.targetChar}'!")
                                        } else {
                                            ttsManager?.speak("यह तो '$opt' है! हमें '${currentRound.targetChar}' खोजना है!")
                                        }
                                    }
                                }
                        ) {
                            // Balloon Oval
                            Box(
                                modifier = Modifier
                                    .size(74.dp, 92.dp)
                                    .clip(RoundedCornerShape(topStart = 37.dp, topEnd = 37.dp, bottomStart = 37.dp, bottomEnd = 37.dp))
                                    .background(
                                        Brush.radialGradient(
                                            listOf(color.copy(alpha = 0.95f), color)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isPopped) "💥" else opt,
                                    fontSize = if (isPopped) 36.sp else 32.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }

                            // Balloon string knot & line
                            Text(text = "🧵", fontSize = 16.sp)
                        }
                    }
                }
            }

            // 4. Bottom Next button
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
                    text = if (isEnglish) "Next Balloon ▶" else "अगला गुब्बारा ▶",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp
                )
            }
        }

        // Celebration Confetti
        if (showConfetti) {
            StarConfettiOverlay(
                onAnimationComplete = { showConfetti = false }
            )
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
