package com.kidslearning.chutki.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidslearning.chutki.audio.HindiTtsManager
import com.kidslearning.chutki.audio.SoundEffectHelper
import com.kidslearning.chutki.data.LearningContentRepository
import com.kidslearning.chutki.model.SubjectType
import com.kidslearning.chutki.theme.CandyOrange
import com.kidslearning.chutki.theme.CorrectGreen
import com.kidslearning.chutki.theme.GoldenStar
import com.kidslearning.chutki.ui.components.ClayButton
import com.kidslearning.chutki.ui.components.ClayCard
import com.kidslearning.chutki.ui.components.StarConfettiOverlay
import com.kidslearning.chutki.ui.components.TeacherAvatar3D
import com.kidslearning.chutki.ui.components.TeacherPose
import kotlinx.coroutines.launch

data class DrawnStroke(
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float = 26f
)

@Composable
fun Level2TracingScreen(
    subject: SubjectType,
    isDarkMode: Boolean,
    ttsManager: HindiTtsManager?,
    onBack: () -> Unit,
    onNavigateToLevel3: () -> Unit,
    onAddReward: (xp: Int, stars: Int) -> Unit = { _, _ -> },
    onLetterTraced: (char: String) -> Unit = {}
) {
    val tracingLetters = remember(subject.id) {
        LearningContentRepository.getTracingLetters(subject.id)
    }

    var letterIndex by remember { mutableIntStateOf(0) }
    val currentLetter = tracingLetters.getOrNull(letterIndex) ?: tracingLetters.first()

    // Multi-stroke drawing state for Slate
    val completedStrokes = remember { mutableStateListOf<DrawnStroke>() }
    val currentStrokePoints = remember { mutableStateListOf<Offset>() }
    var touchFingerPos by remember { mutableStateOf<Offset?>(null) }

    // Demo animation state
    var isDemoPlaying by remember { mutableStateOf(false) }
    val demoAnim = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    // Completion & reward state
    var isCompleted by remember { mutableStateOf(false) }
    var showConfetti by remember { mutableStateOf(false) }

    // Chalk colors
    val chalkColors = listOf(
        Color(0xFFFFD700), // Sunshine Gold
        Color(0xFF00E676), // Lime Green
        Color(0xFF00E5FF), // Cyan Neon
        Color(0xFFFF4081), // Coral Pink
        Color(0xFFFF9100), // Bright Orange
        Color(0xFFFFFFFF)  // White Chalk
    )
    var selectedColor by remember { mutableStateOf(chalkColors[0]) }

    val accentColor = Color(subject.themeColorHex)

    fun resetStateForNewLetter() {
        completedStrokes.clear()
        currentStrokePoints.clear()
        touchFingerPos = null
        isCompleted = false
        showConfetti = false
        isDemoPlaying = false
        ttsManager?.speak(currentLetter.audioPromptHindi)
    }

    // Trigger on letter switch
    LaunchedEffect(letterIndex) {
        resetStateForNewLetter()
    }

    // Function to trigger completion
    fun completeLetter() {
        if (!isCompleted) {
            isCompleted = true
            showConfetti = true
            SoundEffectHelper.playFanfare()
            ttsManager?.celebrateTracingComplete()
            onLetterTraced(currentLetter.char)
            onAddReward(50, 3)
        }
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
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (isDarkMode) Color(0xFF262A56) else Color(0xFFFFECB3))
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "⬅️", fontSize = 18.sp)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "लेवल 2: जादुई स्लेट ✍️",
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        color = if (isDarkMode) Color.White else Color(0xFF263238)
                    )
                    Text(
                        text = "${currentLetter.titleHindi} (${letterIndex + 1}/${tracingLetters.size})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = CandyOrange
                    )
                }

                // Duster / Clear Button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF5252))
                        .clickable {
                            completedStrokes.clear()
                            currentStrokePoints.clear()
                            touchFingerPos = null
                            isCompleted = false
                            SoundEffectHelper.playPopSound()
                            ttsManager?.speak("स्लेट साफ हो गई! फिर से लिखो!")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🧹", fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 2. Priya Didi Hint & Magic Demo Bar
            ClayCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 16.dp,
                elevation = 4.dp,
                bgGradient = Brush.horizontalGradient(
                    if (isDarkMode) listOf(Color(0xFF1F2348), Color(0xFF161936))
                    else listOf(Color(0xFFFFFDE7), Color(0xFFFFF9C4))
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TeacherAvatar3D(
                        size = 72.dp,
                        pose = if (isCompleted) TeacherPose.CHEERING_STARS else TeacherPose.POINTING_AT_CARD,
                        isSpeaking = isDemoPlaying,
                        onClick = {
                            ttsManager?.speak(currentLetter.hintHindi)
                        }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isCompleted) {
                                "“शाबाश! बहुत ही सुंदर लिखा है! 3 चमकते सितारे मिले!”"
                            } else {
                                "“${currentLetter.hintHindi}”"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCompleted) CorrectGreen else (if (isDarkMode) Color.White else Color(0xFF4E342E)),
                            lineHeight = 16.sp,
                            maxLines = 2
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // "जादू देखो (Demo)" Button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isDemoPlaying) Color(0xFFFF9100) else Color(0xFF7E57C2))
                                .clickable(enabled = !isDemoPlaying) {
                                    isDemoPlaying = true
                                    SoundEffectHelper.playStarTwinkle()
                                    ttsManager?.speak("देखो बच्चों, ऐसे सीधी लाइन खींचो और गोल घुमाओ!")
                                    coroutineScope.launch {
                                        demoAnim.snapTo(0f)
                                        demoAnim.animateTo(
                                            targetValue = 1f,
                                            animationSpec = tween(durationMillis = 2400, easing = FastOutSlowInEasing)
                                        )
                                        isDemoPlaying = false
                                        SoundEffectHelper.playCorrectChime()
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isDemoPlaying) "🪄 जादू चल रहा है..." else "🪄 जादू देखो (कैसे लिखें?)",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 3. Full-Size Blackboard Slate Canvas (अक्षर पूरी स्लेट के साइज का)
            ClayCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                cornerRadius = 24.dp,
                elevation = 8.dp,
                bgGradient = Brush.verticalGradient(
                    if (isDarkMode) listOf(Color(0xFF15182D), Color(0xFF0F1122))
                    else listOf(Color(0xFF263238), Color(0xFF1A2327)) // Slate / Chalkboard
                ),
                borderHighlightColor = if (isCompleted) CorrectGreen else accentColor
            ) {
                BoxWithConstraints(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    val slateHeight = maxHeight
                    val slateWidth = maxWidth

                    // Scale the ghost letter dynamically so it fits completely without cutting off at the sides!
                    val minDim = minOf(slateWidth.value, slateHeight.value)
                    val scaleFactor = when {
                        currentLetter.char.length > 1 -> 0.38f // For "10", "अः", "अं"
                        currentLetter.char in listOf("W", "M", "Q", "अ", "आ", "औ", "क्ष", "ज्ञ", "छ", "झ", "ध", "ष", "क", "ख") -> 0.46f
                        else -> 0.52f
                    }
                    val fullLetterFontSize = (minDim * scaleFactor).sp

                    var canvasW by remember { mutableFloatStateOf(0f) }
                    var canvasH by remember { mutableFloatStateOf(0f) }

                    // A. Chalkboard horizontal guidelines
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        canvasW = w
                        canvasH = h

                        // Top line
                        drawLine(
                            color = Color.White.copy(alpha = 0.09f),
                            start = Offset(20f, h * 0.16f),
                            end = Offset(w - 20f, h * 0.16f),
                            strokeWidth = 2f
                        )
                        // Middle line
                        drawLine(
                            color = Color.White.copy(alpha = 0.07f),
                            start = Offset(20f, h * 0.50f),
                            end = Offset(w - 20f, h * 0.50f),
                            strokeWidth = 2f
                        )
                        // Bottom line
                        drawLine(
                            color = Color.White.copy(alpha = 0.09f),
                            start = Offset(20f, h * 0.84f),
                            end = Offset(w - 20f, h * 0.84f),
                            strokeWidth = 2f
                        )
                    }

                    // B. GHOST LETTER TAKING UP FULL SLATE SIZE (बड़ा पूरा अक्षर)
                    Text(
                        text = currentLetter.char,
                        fontSize = fullLetterFontSize,
                        fontWeight = FontWeight.Black,
                        color = Color.White.copy(alpha = 0.22f),
                        textAlign = TextAlign.Center,
                        lineHeight = fullLetterFontSize,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    // C. Interactive Drawing Canvas for Freehand Finger Writing
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(letterIndex) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        currentStrokePoints.clear()
                                        currentStrokePoints.add(offset)
                                        touchFingerPos = offset
                                    },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        val p = change.position
                                        currentStrokePoints.add(p)
                                        touchFingerPos = p
                                    },
                                    onDragEnd = {
                                        if (currentStrokePoints.size > 1) {
                                            completedStrokes.add(
                                                DrawnStroke(
                                                    points = currentStrokePoints.toList(),
                                                    color = selectedColor,
                                                    strokeWidth = 28f
                                                )
                                            )
                                            SoundEffectHelper.playPopSound()
                                        }
                                        currentStrokePoints.clear()
                                        touchFingerPos = null

                                        // Automatic smart check: Only celebrate when the letter is genuinely completed!
                                        if (!isCompleted && canvasW > 0f && canvasH > 0f) {
                                            if (isLetterComplete(completedStrokes, canvasW, canvasH, currentLetter.guideDots)) {
                                                completeLetter()
                                            }
                                        }
                                    },
                                    onDragCancel = {
                                        currentStrokePoints.clear()
                                        touchFingerPos = null
                                    }
                                )
                            }
                    ) {
                        val w = size.width
                        val h = size.height

                        // 1. Draw all completed strokes
                        completedStrokes.forEach { stroke ->
                            if (stroke.points.size > 1) {
                                val path = Path().apply {
                                    moveTo(stroke.points[0].x, stroke.points[0].y)
                                    for (i in 1 until stroke.points.size) {
                                        lineTo(stroke.points[i].x, stroke.points[i].y)
                                    }
                                }

                                // Soft neon glow
                                drawPath(
                                    path = path,
                                    color = stroke.color.copy(alpha = 0.4f),
                                    style = Stroke(
                                        width = stroke.strokeWidth + 16f,
                                        cap = StrokeCap.Round,
                                        join = StrokeJoin.Round
                                    )
                                )

                                // Solid core chalk stroke
                                drawPath(
                                    path = path,
                                    color = stroke.color,
                                    style = Stroke(
                                        width = stroke.strokeWidth,
                                        cap = StrokeCap.Round,
                                        join = StrokeJoin.Round
                                    )
                                )
                            }
                        }

                        // 2. Currently active dragging stroke
                        if (currentStrokePoints.size > 1) {
                            val activePath = Path().apply {
                                moveTo(currentStrokePoints[0].x, currentStrokePoints[0].y)
                                for (i in 1 until currentStrokePoints.size) {
                                    lineTo(currentStrokePoints[i].x, currentStrokePoints[i].y)
                                }
                            }

                            drawPath(
                                path = activePath,
                                color = selectedColor.copy(alpha = 0.45f),
                                style = Stroke(
                                    width = 44f,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )

                            drawPath(
                                path = activePath,
                                color = selectedColor,
                                style = Stroke(
                                    width = 28f,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }

                        // 3. Demo Animation Path
                        if (isDemoPlaying && currentLetter.guideDots.size > 1) {
                            val totalSteps = currentLetter.guideDots.size - 1
                            val currentStepProgress = demoAnim.value * totalSteps
                            val currentIndex = currentStepProgress.toInt().coerceIn(0, totalSteps - 1)
                            val stepFraction = currentStepProgress - currentIndex

                            val p1 = currentLetter.guideDots[currentIndex]
                            val p2 = currentLetter.guideDots[(currentIndex + 1).coerceAtMost(currentLetter.guideDots.size - 1)]

                            val curX = (p1.first + (p2.first - p1.first) * stepFraction) * w
                            val curY = (p1.second + (p2.second - p1.second) * stepFraction) * h

                            // Draw glowing demo head
                            drawCircle(
                                color = Color(0xFFFFD700).copy(alpha = 0.5f),
                                radius = 32f,
                                center = Offset(curX, curY)
                            )
                            drawCircle(
                                color = Color(0xFFFFD700),
                                radius = 18f,
                                center = Offset(curX, curY)
                            )
                        }
                    }

                    // Sparkling pointer at user touch finger
                    touchFingerPos?.let { pos ->
                        Box(
                            modifier = Modifier
                                .offset(
                                    x = (pos.x * 0.38f).dp,
                                    y = (pos.y * 0.38f).dp
                                )
                        ) {
                            Text(text = "✨", fontSize = 26.sp)
                        }
                    }

                    // Helper Prompt when nothing is drawn yet
                    if (completedStrokes.isEmpty() && currentStrokePoints.isEmpty() && !isCompleted) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color.Black.copy(alpha = 0.55f))
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "👆 उंगली से पूरे अक्षर पर लिखो!",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD54F)
                                )
                            }
                        }
                    }

                    // Completed Banner
                    if (isCompleted) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(CorrectGreen)
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "⭐ ⭐ ⭐ बहुत सुंदर लिखा!",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 4. Color Palette & "मैंने लिख लिया! ✅" Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Chalk Color Selector
                Row(verticalAlignment = Alignment.CenterVertically) {
                    chalkColors.forEach { color ->
                        val isSelected = color == selectedColor
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(if (isSelected) 32.dp else 24.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = Color.White,
                                    shape = CircleShape
                                )
                                .clickable {
                                    selectedColor = color
                                    SoundEffectHelper.playPopSound()
                                }
                        )
                    }
                }

                // Instant Validation Button: "मैंने लिख लिया! ✅"
                ClayButton(
                    onClick = {
                        completeLetter()
                    },
                    modifier = Modifier.height(40.dp),
                    primaryColor = if (isCompleted) CorrectGreen else GoldenStar,
                    shadowColor = Color(0xFFD4AC0D),
                    topHighlightColor = Color(0xFFFFF176)
                ) {
                    Text(
                        text = if (isCompleted) "शाबाश! ✅" else "मैंने लिखा! ✅",
                        color = Color(0xFF3E2723),
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 5. Navigation Controls (पिछला अक्षर & अगला अक्षर)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ClayButton(
                    onClick = {
                        if (letterIndex > 0) {
                            letterIndex--
                        }
                    },
                    enabled = letterIndex > 0,
                    modifier = Modifier.weight(1f),
                    primaryColor = Color(0xFF78909C),
                    shadowColor = Color(0xFF455A64),
                    topHighlightColor = Color(0xFFB0BEC5)
                ) {
                    Text(
                        text = "◀ पिछला",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                if (letterIndex < tracingLetters.size - 1) {
                    ClayButton(
                        onClick = {
                            letterIndex++
                        },
                        modifier = Modifier.weight(1.2f),
                        primaryColor = accentColor,
                        shadowColor = accentColor.copy(alpha = 0.7f),
                        topHighlightColor = accentColor.copy(alpha = 0.9f)
                    ) {
                        Text(
                            text = "अगला अक्षर ▶",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    ClayButton(
                        onClick = onNavigateToLevel3,
                        modifier = Modifier.weight(1.3f),
                        primaryColor = CorrectGreen,
                        shadowColor = Color(0xFF27AE60),
                        topHighlightColor = Color(0xFF58D68D)
                    ) {
                        Text(
                            text = "लेवल 3: शब्द जोड़ो 🧩",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // Confetti Celebration
        StarConfettiOverlay(
            trigger = showConfetti,
            onFinished = { showConfetti = false }
        )
    }
}

/**
 * Intelligent completion check:
 * Ensures the child has genuinely written the letter (adequate stroke distance,
 * horizontal and vertical coverage across the slate, and hitting key letter checkpoints)
 * before triggering "शाबाश" and victory fanfare.
 */
private fun isLetterComplete(
    strokes: List<DrawnStroke>,
    w: Float,
    h: Float,
    dots: List<Pair<Float, Float>>
): Boolean {
    if (strokes.isEmpty() || w <= 0f || h <= 0f) return false
    val allPoints = strokes.flatMap { it.points }
    if (allPoints.size < 18) return false

    // Total distance of strokes drawn
    var totalDistance = 0f
    for (stroke in strokes) {
        for (i in 0 until stroke.points.size - 1) {
            totalDistance += (stroke.points[i + 1] - stroke.points[i]).getDistance()
        }
    }
    // Must draw at least 65% of canvas width in total path length
    if (totalDistance < w * 0.65f) return false

    // Bounding box coverage
    val minX = allPoints.minOf { it.x }
    val maxX = allPoints.maxOf { it.x }
    val minY = allPoints.minOf { it.y }
    val maxY = allPoints.maxOf { it.y }

    val widthSpan = maxX - minX
    val heightSpan = maxY - minY

    // Child must write across at least 24% of width and 26% of height
    if (widthSpan < w * 0.24f || heightSpan < h * 0.26f) return false

    // Check guide dots coverage if present
    if (dots.isNotEmpty()) {
        val hitCount = dots.count { (nx, ny) ->
            val target = Offset(nx * w, ny * h)
            allPoints.any { p -> (p - target).getDistance() < 75f }
        }
        val requiredHits = (dots.size * 0.45f).toInt().coerceAtLeast(2)
        if (hitCount < requiredHits) return false
    }

    return true
}

