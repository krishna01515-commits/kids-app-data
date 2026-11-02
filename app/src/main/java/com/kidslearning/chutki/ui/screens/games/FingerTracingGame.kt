package com.kidslearning.chutki.ui.screens.games

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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidslearning.chutki.audio.HindiTtsManager
import com.kidslearning.chutki.audio.SoundEffectHelper
import com.kidslearning.chutki.data.ContentRepository
import com.kidslearning.chutki.data.TracingRoundData
import com.kidslearning.chutki.theme.CandyOrange
import com.kidslearning.chutki.theme.CorrectGreen
import com.kidslearning.chutki.ui.components.ClayButton
import com.kidslearning.chutki.ui.components.ClayCard
import com.kidslearning.chutki.ui.components.LevelMilestoneCelebration
import com.kidslearning.chutki.ui.components.StarConfettiOverlay
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class MiniStroke(
    val points: List<Offset>,
    val color: Color,
    val width: Float = 24f
)

@Composable
fun FingerTracingGame(
    isDarkMode: Boolean,
    isEnglish: Boolean = false,
    ttsManager: HindiTtsManager?,
    onBack: () -> Unit,
    onReward: (xp: Int, stars: Int) -> Unit
) {
    var gameLevel by remember { mutableIntStateOf(1) }
    var lettersCompletedCount by remember { mutableIntStateOf(0) }
    var currentStreak by remember { mutableIntStateOf(0) }

    // Buffer of last 4 used item IDs to prevent immediate repeats
    val recentBuffer = remember { mutableStateListOf<Int>() }

    var currentRound by remember {
        mutableStateOf(ContentRepository.generateTracingRound(gameLevel, recentBuffer, isEnglish))
    }

    val chalkColors = listOf(
        Color(0xFFFFD700), // Gold
        Color(0xFF00E676), // Green
        Color(0xFF00E5FF), // Cyan
        Color(0xFFFF4081), // Pink
        Color(0xFFFFFFFF)  // White Chalk
    )
    var selectedColor by remember { mutableStateOf(chalkColors[0]) }

    val strokes = remember(currentRound.targetItem.id) { mutableStateListOf<MiniStroke>() }
    val activePoints = remember { mutableStateListOf<Offset>() }
    var isCompleted by remember { mutableStateOf(false) }
    var showConfetti by remember { mutableStateOf(false) }
    var showMilestone by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var lastClickTimestamp by remember { mutableStateOf(0L) }
    val scope = rememberCoroutineScope()

    fun loadNextRound() {
        strokes.clear()
        activePoints.clear()
        isCompleted = false
        showConfetti = false
        isProcessing = false

        // Maintain buffer of last 4 used items
        if (!recentBuffer.contains(currentRound.targetItem.id)) {
            recentBuffer.add(currentRound.targetItem.id)
            if (recentBuffer.size > 4) recentBuffer.removeAt(0)
        }

        currentRound = ContentRepository.generateTracingRound(gameLevel, recentBuffer, isEnglish)

        // Debounce & Stop TTS before speaking new prompt
        ttsManager?.stop()
        val prompt = if (isEnglish) currentRound.promptEnglish else currentRound.promptHindi
        ttsManager?.speak(prompt)
    }

    LaunchedEffect(currentRound.targetItem.id, isEnglish) {
        strokes.clear()
        activePoints.clear()
        isCompleted = false
        showConfetti = false

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
            .padding(14.dp)
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
                        text = if (isEnglish) "✍️ Letter Tracing Canvas" else "✍️ अक्षर ट्रेसिंग स्लेट",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = if (isDarkMode) Color.White else Color(0xFF263238)
                    )
                    Text(
                        text = if (isEnglish) "Item #${lettersCompletedCount + 1} • Level $gameLevel" else "आइटम #${lettersCompletedCount + 1} • लेवल $gameLevel",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CandyOrange
                    )
                }

                // Duster
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF5252))
                        .clickable {
                            val now = System.currentTimeMillis()
                            if (now - lastClickTimestamp > 300L) {
                                lastClickTimestamp = now
                                strokes.clear()
                                activePoints.clear()
                                isCompleted = false
                                SoundEffectHelper.playPopSound()
                                ttsManager?.stop()
                                if (isEnglish) {
                                    ttsManager?.speak("Board cleaned! Trace again!")
                                } else {
                                    ttsManager?.speak("स्लेट साफ हो गई! फिर से लिखो!")
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🧹", fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Subtitle banner with item emoji and name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                Text(text = currentRound.emoji, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEnglish) currentRound.nameEnglish else currentRound.nameHindi,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (isDarkMode) Color(0xFFA5B0E0) else Color(0xFF37474F)
                )
            }

            // 2. Blackboard Canvas with Dotted Letter Outline
            ClayCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                cornerRadius = 24.dp,
                elevation = 8.dp,
                bgGradient = Brush.verticalGradient(
                    if (isDarkMode) listOf(Color(0xFF15182D), Color(0xFF0F1122))
                    else listOf(Color(0xFF263238), Color(0xFF1A2327))
                ),
                borderHighlightColor = if (isCompleted) CorrectGreen else selectedColor
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Huge Watermark Letter in center
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentRound.charToTrace,
                            fontSize = 200.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White.copy(alpha = 0.22f)
                        )
                    }

                    // Canvas drawing layer
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(currentRound.targetItem.id) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        activePoints.clear()
                                        activePoints.add(offset)
                                    },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        activePoints.add(change.position)
                                    },
                                    onDragEnd = {
                                        if (activePoints.isNotEmpty()) {
                                            strokes.add(
                                                MiniStroke(
                                                    points = activePoints.toList(),
                                                    color = selectedColor
                                                )
                                            )
                                            activePoints.clear()
                                        }
                                    }
                                )
                            }
                    ) {
                        strokes.forEach { s ->
                            if (s.points.size > 1) {
                                val path = Path().apply {
                                    moveTo(s.points[0].x, s.points[0].y)
                                    for (i in 1 until s.points.size) {
                                        lineTo(s.points[i].x, s.points[i].y)
                                    }
                                }
                                drawPath(
                                    path = path,
                                    color = s.color,
                                    style = Stroke(
                                        width = s.width,
                                        cap = StrokeCap.Round,
                                        join = StrokeJoin.Round
                                    )
                                )
                            }
                        }

                        if (activePoints.size > 1) {
                            val activePath = Path().apply {
                                moveTo(activePoints[0].x, activePoints[0].y)
                                for (i in 1 until activePoints.size) {
                                    lineTo(activePoints[i].x, activePoints[i].y)
                                }
                            }
                            drawPath(
                                path = activePath,
                                color = selectedColor,
                                style = Stroke(
                                    width = 24f,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }

                    if (isCompleted) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(CorrectGreen)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (isEnglish) "⭐ 3 Stars Earned! ⭐" else "⭐ शाबाश! 3 सितारे ⭐",
                                color = Color.White,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Chalk Colors
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    chalkColors.forEach { col ->
                        val isChosen = selectedColor == col
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(if (isChosen) 32.dp else 24.dp)
                                .clip(CircleShape)
                                .background(col)
                                .border(
                                    width = if (isChosen) 2.5.dp else 1.dp,
                                    color = Color.White,
                                    shape = CircleShape
                                )
                                .clickable {
                                    selectedColor = col
                                    SoundEffectHelper.playPopSound()
                                }
                        )
                    }
                }

                // Debounced Submit Button
                ClayButton(
                    onClick = {
                        val now = System.currentTimeMillis()
                        if (now - lastClickTimestamp < 600L || isProcessing) return@ClayButton
                        lastClickTimestamp = now

                        if (!isCompleted) {
                            isProcessing = true
                            isCompleted = true
                            showConfetti = true
                            lettersCompletedCount++
                            currentStreak++

                            SoundEffectHelper.playFanfare()
                            SoundEffectHelper.playComboSound(currentStreak)

                            ttsManager?.stop()
                            ttsManager?.celebrateTracingComplete(isEnglish)
                            onReward(40, 3)

                            scope.launch {
                                // Every 5 correct answers: level up with 2-second celebration
                                if (lettersCompletedCount % 5 == 0) {
                                    gameLevel++
                                    showMilestone = true
                                    delay(2000)
                                    showMilestone = false
                                } else {
                                    delay(1200)
                                }
                                loadNextRound()
                            }
                        }
                    },
                    modifier = Modifier.height(40.dp),
                    primaryColor = CorrectGreen,
                    shadowColor = Color(0xFF1B5E20),
                    topHighlightColor = Color(0xFF69F0AE)
                ) {
                    Text(
                        text = if (isEnglish) "I Wrote It! ✅" else "मैंने लिखा! ✅",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4. Next Letter button (Infinite loop from ContentRepository)
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
                    text = if (isEnglish) "Next Letter ▶" else "अगला अक्षर ▶",
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
