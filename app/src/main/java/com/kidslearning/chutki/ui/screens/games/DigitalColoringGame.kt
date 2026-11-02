package com.kidslearning.chutki.ui.screens.games

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import com.kidslearning.chutki.theme.CandyOrange
import com.kidslearning.chutki.theme.CandyYellow
import com.kidslearning.chutki.theme.CorrectGreen
import com.kidslearning.chutki.ui.components.ClayButton
import com.kidslearning.chutki.ui.components.ClayCard
import com.kidslearning.chutki.ui.components.StarConfettiOverlay
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ColorStroke(
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float = 24f
)

data class OutlineSketch(
    val titleHindi: String,
    val titleEnglish: String,
    val emoji: String,
    val outlineDescHindi: String,
    val outlineDescEnglish: String
)

@Composable
fun DigitalColoringGame(
    isDarkMode: Boolean,
    isEnglish: Boolean = false,
    ttsManager: HindiTtsManager?,
    onBack: () -> Unit,
    onReward: (xp: Int, stars: Int) -> Unit
) {
    val sketches = remember {
        listOf(
            OutlineSketch("सुंदर तितली", "Beautiful Butterfly", "🦋", "तितली में रंग भरो!", "Color the butterfly!"),
            OutlineSketch("रसीला सेब", "Sweet Apple", "🍎", "सेब में लाल रंग भरो!", "Color the red apple!"),
            OutlineSketch("तेज कार", "Sports Car", "🚗", "कार में सुंदर रंग भरो!", "Paint the car!"),
            OutlineSketch("प्यारा फूल", "Pretty Flower", "🌸", "फूल की पंखुड़ियों में रंग भरो!", "Color the flower petals!"),
            OutlineSketch("छोटा घर", "Sweet Home", "🏠", "घर में रंग भरो!", "Color the home!"),
            OutlineSketch("बब्बर शेर", "Mighty Lion", "🦁", "शेर में पीला और भूरा रंग भरो!", "Color the mighty lion!"),
            OutlineSketch("अंतरिक्ष रॉकेट", "Space Rocket", "🚀", "रॉकेट में रंग भरो!", "Color the space rocket!"),
            OutlineSketch("चंचल मछली", "Playful Fish", "🐟", "मछली में नीला रंग भरो!", "Color the swimming fish!"),
            OutlineSketch("चमकता सूरज", "Bright Sun", "☀️", "सूरज में पीला रंग भरो!", "Color the golden sun!"),
            OutlineSketch("हरा-भरा पेड़", "Green Tree", "🌳", "पेड़ में हरा रंग भरो!", "Color the leafy tree!")
        )
    }

    var sketchIdx by remember { mutableIntStateOf(0) }
    val currentSketch = sketches[sketchIdx % sketches.size]

    val palette = listOf(
        Color(0xFFFF1744), // Red
        Color(0xFFFFEA00), // Yellow
        Color(0xFF00E676), // Green
        Color(0xFF2979FF), // Blue
        Color(0xFFFF9100), // Orange
        Color(0xFFD500F9), // Purple
        Color(0xFFFF4081), // Pink
        Color(0xFF8D6E63), // Brown
        Color(0xFF00BCD4), // Cyan
        Color(0xFFFFFFFF)  // White Eraser
    )

    var selectedColor by remember { mutableStateOf(palette[0]) }
    val strokes = remember(sketchIdx) { mutableStateListOf<ColorStroke>() }
    val currentPoints = remember { mutableStateListOf<Offset>() }
    var showConfetti by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(sketchIdx) {
        ttsManager?.stop()
        if (isEnglish) {
            ttsManager?.speak("${currentSketch.titleEnglish}! ${currentSketch.outlineDescEnglish}")
        } else {
            ttsManager?.speak("${currentSketch.titleHindi}! ${currentSketch.outlineDescHindi}")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (isDarkMode) Brush.verticalGradient(listOf(Color(0xFF13142B), Color(0xFF1E2142)))
                else Brush.verticalGradient(listOf(Color(0xFFFFFDF5), Color(0xFFF3E5F5)))
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
                        text = if (isEnglish) "🎨 Digital Coloring" else "🎨 रंग भरो (Coloring)",
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        color = if (isDarkMode) Color.White else Color(0xFF263238)
                    )
                    Text(
                        text = if (isEnglish) currentSketch.titleEnglish else currentSketch.titleHindi,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CandyOrange
                    )
                }

                // Clear/Eraser
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF5252))
                        .clickable {
                            strokes.clear()
                            currentPoints.clear()
                            SoundEffectHelper.playPopSound()
                            ttsManager?.stop()
                            if (isEnglish) {
                                ttsManager?.speak("Canvas cleared! Start painting again!")
                            } else {
                                ttsManager?.speak("कैनवास साफ हो गया! फिर से रंग भरो!")
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🧹", fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Drawing Canvas with Outline watermark
            ClayCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                cornerRadius = 24.dp,
                elevation = 8.dp,
                bgGradient = Brush.verticalGradient(
                    if (isDarkMode) listOf(Color(0xFF1C2038), Color(0xFF131526))
                    else listOf(Color.White, Color(0xFFFFFDE7))
                ),
                borderHighlightColor = selectedColor
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Big Watermark Outline in background
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentSketch.emoji,
                            fontSize = 160.sp,
                            color = Color.Black.copy(alpha = 0.12f)
                        )
                    }

                    // Interactive Drawing Canvas
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
                                            strokes.add(
                                                ColorStroke(
                                                    points = currentPoints.toList(),
                                                    color = selectedColor
                                                )
                                            )
                                            currentPoints.clear()
                                        }
                                    }
                                )
                            }
                    ) {
                        // Draw completed strokes
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
                                        width = s.strokeWidth,
                                        cap = StrokeCap.Round,
                                        join = StrokeJoin.Round
                                    )
                                )
                            }
                        }

                        // Draw currently active stroke
                        if (currentPoints.size > 1) {
                            val activePath = Path().apply {
                                moveTo(currentPoints[0].x, currentPoints[0].y)
                                for (i in 1 until currentPoints.size) {
                                    lineTo(currentPoints[i].x, currentPoints[i].y)
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
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Color Palette
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                palette.forEach { col ->
                    val isChosen = selectedColor == col
                    Box(
                        modifier = Modifier
                            .size(if (isChosen) 36.dp else 26.dp)
                            .clip(CircleShape)
                            .background(col)
                            .border(
                                width = if (isChosen) 3.dp else 1.5.dp,
                                color = if (isChosen) CandyOrange else Color.Gray,
                                shape = CircleShape
                            )
                            .clickable {
                                selectedColor = col
                                SoundEffectHelper.playPopSound()
                            }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4. Action Buttons: "रंग भर लिया! ✅" and "अगला चित्र ▶"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ClayButton(
                    onClick = {
                        if (isProcessing) return@ClayButton
                        isProcessing = true
                        showConfetti = true
                        SoundEffectHelper.playFanfare()
                        ttsManager?.stop()
                        if (isEnglish) {
                            ttsManager?.speak("Wow! Beautiful painting! Great colors!")
                        } else {
                            ttsManager?.speak("अरे वाह! आपने बहुत सुंदर रंग भरे हैं! आपकी पेंटिंग शानदार है!")
                        }
                        onReward(35, 3)

                        scope.launch {
                            delay(1500)
                            isProcessing = false
                        }
                    },
                    modifier = Modifier.weight(1f),
                    primaryColor = CorrectGreen,
                    shadowColor = Color(0xFF1B5E20),
                    topHighlightColor = Color(0xFF69F0AE)
                ) {
                    Text(
                        text = if (isEnglish) "Finished! 🎨" else "रंग भर लिया! 🎨",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }

                ClayButton(
                    onClick = {
                        sketchIdx = (sketchIdx + 1) % sketches.size
                        SoundEffectHelper.playPopSound()
                    },
                    modifier = Modifier.weight(1f),
                    primaryColor = CandyOrange,
                    shadowColor = Color(0xFFD84315),
                    topHighlightColor = Color(0xFFFFB74D)
                ) {
                    Text(
                        text = if (isEnglish) "Next Picture ▶" else "अगला चित्र ▶",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
            }
        }

        if (showConfetti) {
            StarConfettiOverlay(onAnimationComplete = { showConfetti = false })
        }
    }
}
