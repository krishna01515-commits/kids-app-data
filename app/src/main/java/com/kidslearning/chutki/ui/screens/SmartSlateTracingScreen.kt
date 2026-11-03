package com.kidslearning.chutki.ui.screens

import android.graphics.Paint
import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
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
import com.kidslearning.chutki.model.TracingLetter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class TracingStroke(
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float = 36f
)

data class TracingValidationResult(
    val isCorrect: Boolean,
    val message: String
)

@Composable
fun SmartSlateTracingScreen(
    currentLetter: String = "A",
    letterHint: String = "ऊपर से दो तिरछी लाइन (/ \\) खींचें और बीच में सीधी लाइन से जोड़ें।",
    onBackClick: () -> Unit = {},
    onNextLetter: () -> Unit = {},
    subject: SubjectType? = null,
    ttsManager: HindiTtsManager? = null,
    onNavigateToLevel3: (() -> Unit)? = null,
    onLetterTraced: ((String) -> Unit)? = null
) {
    // 1. Determine active subject (Default to Hindi if not specified)
    val activeSubject = subject ?: when {
        currentLetter.all { it in '0'..'9' } -> SubjectType.MATH
        currentLetter.firstOrNull()?.let { it in 'A'..'Z' || it in 'a'..'z' } == true -> SubjectType.ENGLISH
        else -> SubjectType.HINDI
    }

    // 2. Fetch subject-specific letters from repository
    val allSubjectTracingLetters = remember(activeSubject.id) {
        LearningContentRepository.getTracingLetters(activeSubject.id)
    }

    // 3. Define subject-locked sub-category tabs
    var selectedSubTabIndex by remember(activeSubject.id) { mutableIntStateOf(0) }

    val subTabTitles = when (activeSubject) {
        SubjectType.HINDI -> listOf("सभी (अ-ज्ञ)", "स्वर (अ-अः)", "व्यंजन (क-ज्ञ)")
        SubjectType.ENGLISH -> listOf("Capital (A-Z)", "Small (a-z)")
        SubjectType.MATH -> listOf("1 से 20", "21 से 50", "51 से 100")
    }

    // 4. Filter letters strictly based on subject and selected sub-tab
    val filteredLetters = remember(activeSubject.id, selectedSubTabIndex, allSubjectTracingLetters) {
        when (activeSubject) {
            SubjectType.HINDI -> {
                val swarList = listOf("अ", "आ", "इ", "ई", "उ", "ऊ", "ऋ", "ए", "ऐ", "ओ", "औ", "अं", "अः")
                when (selectedSubTabIndex) {
                    1 -> allSubjectTracingLetters.filter { it.char in swarList }
                    2 -> allSubjectTracingLetters.filter { it.char !in swarList }
                    else -> allSubjectTracingLetters
                }
            }
            SubjectType.ENGLISH -> {
                if (selectedSubTabIndex == 1) {
                    // Small letters (a-z)
                    ('a'..'z').map { smallChar ->
                        val upperChar = smallChar.uppercase()
                        val parentItem = allSubjectTracingLetters.find { it.char == upperChar }
                        TracingLetter(
                            char = smallChar.toString(),
                            subjectId = "english",
                            titleHindi = "Letter '$smallChar' लिखो",
                            hintHindi = "छोटा '$smallChar' लिखें। " + (parentItem?.hintHindi ?: ""),
                            guideDots = parentItem?.guideDots ?: emptyList(),
                            audioPromptHindi = "Letter '$smallChar' लिखो!"
                        )
                    }
                } else {
                    allSubjectTracingLetters
                }
            }
            SubjectType.MATH -> {
                when (selectedSubTabIndex) {
                    0 -> allSubjectTracingLetters.take(20)
                    1 -> allSubjectTracingLetters.drop(20).take(30)
                    else -> allSubjectTracingLetters.drop(50)
                }
            }
        }
    }

    // 5. Letter navigation index
    var letterIndex by remember(activeSubject.id, selectedSubTabIndex) {
        val initialIdx = filteredLetters.indexOfFirst { it.char == currentLetter }
        mutableIntStateOf(if (initialIdx >= 0) initialIdx else 0)
    }

    val currentItem = filteredLetters.getOrElse(letterIndex) {
        filteredLetters.firstOrNull() ?: TracingLetter(
            char = currentLetter,
            subjectId = activeSubject.id,
            titleHindi = "अक्षर '$currentLetter' लिखो",
            hintHindi = letterHint,
            guideDots = emptyList(),
            audioPromptHindi = "लिखो '$currentLetter'"
        )
    }

    val activeChar = currentItem.char
    val activeHint = currentItem.hintHindi.ifBlank { letterHint }
    val guideDots = currentItem.guideDots

    // Drawing state
    val userStrokes = remember { mutableStateListOf<TracingStroke>() }
    var currentPoints = remember { mutableStateListOf<Offset>() }
    var selectedColor by remember { mutableStateOf(Color(0xFFFFEB3B)) } // Magic yellow chalk

    // Canvas size for accuracy calculations
    var canvasWidth by remember { mutableFloatStateOf(0f) }
    var canvasHeight by remember { mutableFloatStateOf(0f) }

    // Feedback states
    var feedbackMessage by remember { mutableStateOf("उंगली से डॉट्स को मिलाते हुए अक्षर बनाओ!") }
    var feedbackStatus by remember { mutableStateOf<Boolean?>(null) } // null: idle, true: correct, false: wrong
    var showDemoGuide by remember { mutableStateOf(false) }

    // Demo wand animation
    val demoProgress = remember { Animatable(0f) }
    LaunchedEffect(showDemoGuide, activeChar) {
        if (showDemoGuide && guideDots.size > 1) {
            while (showDemoGuide) {
                demoProgress.snapTo(0f)
                demoProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 2200, easing = LinearEasing)
                )
                delay(400)
            }
        }
    }

    // Announce letter on change and clear old strokes
    LaunchedEffect(activeChar) {
        userStrokes.clear()
        currentPoints.clear()
        feedbackStatus = null
        feedbackMessage = "उंगली से डॉट्स को मिलाते हुए अक्षर बनाओ!"
        val prompt = when (activeSubject) {
            SubjectType.HINDI -> "जादुई स्लेट पर लिखो अक्षर '$activeChar'"
            SubjectType.ENGLISH -> "Write letter '$activeChar' on the slate"
            SubjectType.MATH -> "जादुई स्लेट पर लिखो संख्या '$activeChar'"
        }
        ttsManager?.speak(prompt)
    }

    // Color Palette
    val chalkColors = listOf(
        Color(0xFFFFEB3B), // Yellow
        Color(0xFF00E676), // Green
        Color(0xFF00E5FF), // Cyan
        Color(0xFFFF4081), // Pink
        Color(0xFFFF9100), // Orange
        Color(0xFFFFFFFF)  // White
    )

    // Check which checkpoints have been hit by the user's chalk strokes
    val hitCheckpoints = remember(userStrokes.size, currentPoints.size, canvasWidth, canvasHeight) {
        if (canvasWidth <= 0f || canvasHeight <= 0f || guideDots.isEmpty()) {
            emptyList()
        } else {
            val allPts = userStrokes.flatMap { it.points } + currentPoints
            val threshold = (minOf(canvasWidth, canvasHeight) * 0.15f).coerceIn(65f, 105f)
            guideDots.map { (nx, ny) ->
                val target = Offset(nx * canvasWidth, ny * canvasHeight)
                allPts.any { p -> (p - target).getDistance() <= threshold }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF131722)) // Deep dark slate background
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- 1. Top Bar ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFF1E88E5))
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val headerTitle = when (activeSubject) {
                    SubjectType.HINDI -> "हिंदी वर्णमाला - जादुई स्लेट ✍️"
                    SubjectType.ENGLISH -> "English ABC - जादुई स्लेट ✍️"
                    SubjectType.MATH -> "गणित 123 - जादुई स्लेट ✍️"
                }
                Text(
                    text = headerTitle,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${if (activeSubject == SubjectType.MATH) "संख्या" else "अक्षर"} '$activeChar' (${letterIndex + 1}/${filteredLetters.size})",
                    color = Color(0xFFFFB300),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Eraser button
            IconButton(
                onClick = {
                    SoundEffectHelper.playPopSound()
                    userStrokes.clear()
                    currentPoints.clear()
                    feedbackStatus = null
                    feedbackMessage = "उंगली से डॉट्स को मिलाते हुए अक्षर बनाओ!"
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFFFF5252))
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Clear", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- 2. Subject-Specific Category Tabs ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            subTabTitles.forEachIndexed { idx, title ->
                val isSelected = selectedSubTabIndex == idx
                Button(
                    onClick = {
                        if (selectedSubTabIndex != idx) {
                            selectedSubTabIndex = idx
                            letterIndex = 0
                            userStrokes.clear()
                            currentPoints.clear()
                            feedbackStatus = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) Color(0xFF673AB7) else Color(0xFF2C3246)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        color = Color.White,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- 3. Priya Didi Guidance Box ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2337)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFF3F51B5))
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = "“$activeHint”",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = {
                        showDemoGuide = !showDemoGuide
                        if (showDemoGuide) {
                            ttsManager?.speak("जादू देखो! पीले डॉट्स को मिलाते हुए उंगली फेरें!")
                        }
                    }) {
                        Text(
                            text = if (showDemoGuide) "🪄 गाइड छिपाएं" else "🪄 जादू देखो (कैसे लिखें?)",
                            color = Color(0xFF80D8FF),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Status Indicator
                    if (feedbackStatus != null) {
                        Text(
                            text = if (feedbackStatus == true) "✅ शाबाश! बहुत अच्छा" else "❌ गलत है! फिर से लिखें",
                            color = if (feedbackStatus == true) Color(0xFF00E676) else Color(0xFFFF5252),
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- 4. Black Slate Canvas with Dotted Letter & Touch Detector ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF0D0F15))
                .border(3.dp, Color(0xFFFF9800), RoundedCornerShape(24.dp))
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(activeChar) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                currentPoints = mutableStateListOf(offset)
                            },
                            onDrag = { change, _ ->
                                currentPoints.add(change.position)
                            },
                            onDragEnd = {
                                if (currentPoints.isNotEmpty()) {
                                    userStrokes.add(
                                        TracingStroke(
                                            points = currentPoints.toList(),
                                            color = selectedColor
                                        )
                                    )
                                    currentPoints = mutableStateListOf()
                                }
                            }
                        )
                    }
            ) {
                canvasWidth = size.width
                canvasHeight = size.height

                val w = size.width
                val h = size.height

                // Draw Slate Guide Lines
                drawLine(
                    color = Color(0x33FFFFFF),
                    start = Offset(30f, h * 0.25f),
                    end = Offset(w - 30f, h * 0.25f),
                    strokeWidth = 2f
                )
                drawLine(
                    color = Color(0x33FFFFFF),
                    start = Offset(30f, h * 0.50f),
                    end = Offset(w - 30f, h * 0.50f),
                    strokeWidth = 2f
                )
                drawLine(
                    color = Color(0x33FFFFFF),
                    start = Offset(30f, h * 0.75f),
                    end = Offset(w - 30f, h * 0.75f),
                    strokeWidth = 2f
                )

                // Dotted Template Character (Native Canvas Text to Path)
                val fontSize = if (activeChar.length > 2) h * 0.36f else if (activeChar.length > 1) h * 0.44f else h * 0.55f
                val textPaint = Paint().apply {
                    textSize = fontSize
                    color = android.graphics.Color.argb(80, 255, 255, 255)
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                    style = Paint.Style.STROKE
                    strokeWidth = 14f
                    pathEffect = android.graphics.DashPathEffect(floatArrayOf(24f, 18f), 0f)
                }

                drawContext.canvas.nativeCanvas.drawText(
                    activeChar,
                    w / 2,
                    h * 0.68f,
                    textPaint
                )

                // Draw Interactive Checkpoints (Turn Green when touched!)
                if (guideDots.isNotEmpty()) {
                    guideDots.forEachIndexed { i, (nx, ny) ->
                        val cx = nx * w
                        val cy = ny * h
                        val isHit = hitCheckpoints.getOrElse(i) { false }

                        // Outer halo
                        drawCircle(
                            color = if (isHit) Color(0x4400E676) else Color(0x33FFD54F),
                            radius = if (isHit) 12.dp.toPx() else 8.dp.toPx(),
                            center = Offset(cx, cy)
                        )
                        // Core dot
                        drawCircle(
                            color = if (isHit) Color(0xFF00E676) else Color(0xFFFFD54F),
                            radius = if (isHit) 6.dp.toPx() else 5.dp.toPx(),
                            center = Offset(cx, cy)
                        )
                        // White reflection inside hit dots
                        if (isHit) {
                            drawCircle(
                                color = Color.White,
                                radius = 2.5.dp.toPx(),
                                center = Offset(cx, cy)
                            )
                        }
                    }
                }

                // Draw Completed Strokes
                for (stroke in userStrokes) {
                    val path = Path()
                    stroke.points.forEachIndexed { index, point ->
                        if (index == 0) path.moveTo(point.x, point.y)
                        else path.lineTo(point.x, point.y)
                    }
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

                // Draw Current Live Stroke
                if (currentPoints.isNotEmpty()) {
                    val livePath = Path()
                    currentPoints.forEachIndexed { index, point ->
                        if (index == 0) livePath.moveTo(point.x, point.y)
                        else livePath.lineTo(point.x, point.y)
                    }
                    drawPath(
                        path = livePath,
                        color = selectedColor,
                        style = Stroke(
                            width = 36f,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }

                // Animated Magic Wand (When 'जादू देखो' is active)
                if (showDemoGuide && guideDots.size > 1) {
                    val totalSegments = guideDots.size - 1
                    val currentStep = (demoProgress.value * totalSegments).toInt().coerceIn(0, totalSegments - 1)
                    val frac = (demoProgress.value * totalSegments) - currentStep
                    val (x1, y1) = guideDots[currentStep]
                    val (x2, y2) = guideDots[(currentStep + 1).coerceAtMost(guideDots.size - 1)]
                    val wandX = (x1 + (x2 - x1) * frac) * w
                    val wandY = (y1 + (y2 - y1) * frac) * h

                    // Glowing magic wand tip
                    drawCircle(
                        color = Color(0x66FFEB3B),
                        radius = 20.dp.toPx(),
                        center = Offset(wandX, wandY)
                    )
                    drawCircle(
                        color = Color(0xFFFFD54F),
                        radius = 12.dp.toPx(),
                        center = Offset(wandX, wandY)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 6.dp.toPx(),
                        center = Offset(wandX, wandY)
                    )
                }
            }

            // Text Hint when guide is shown
            if (showDemoGuide) {
                Text(
                    text = "👉 चमकते डॉट्स को मिलाते हुए लिखें",
                    color = Color(0xFFFFEB3B),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 12.dp)
                        .background(Color(0xDD000000), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- 5. Chalk Color Palette & Smart Auto-Check Button ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                chalkColors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (selectedColor == color) 3.dp else 1.dp,
                                color = if (selectedColor == color) Color.White else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { selectedColor = color }
                    )
                }
            }

            // Smart Auto-Check Button
            Button(
                onClick = {
                    val allPoints = userStrokes.flatMap { it.points } + currentPoints

                    val validation = validateSmartTracing(
                        userPoints = allPoints,
                        guideDots = guideDots,
                        canvasWidth = canvasWidth,
                        canvasHeight = canvasHeight,
                        letter = activeChar
                    )

                    if (validation.isCorrect) {
                        feedbackStatus = true
                        feedbackMessage = validation.message
                        showDemoGuide = false
                        SoundEffectHelper.playCorrectChime()
                        ttsManager?.speak("शाबाश! बिल्कुल सही लिखा!")
                        onLetterTraced?.invoke(activeChar)
                    } else {
                        feedbackStatus = false
                        feedbackMessage = validation.message
                        showDemoGuide = true
                        SoundEffectHelper.playSoftTryAgain()
                        ttsManager?.speak(validation.message)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(4.dp))
                Text("चेक करें", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- 6. Navigation Buttons ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    if (letterIndex > 0) {
                        letterIndex--
                    } else {
                        onBackClick()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37474F)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("◀ पिछला", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = {
                    if (letterIndex < filteredLetters.size - 1) {
                        letterIndex++
                        onNextLetter()
                    } else {
                        onNavigateToLevel3?.invoke() ?: onNextLetter()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6D00)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("अगला ▶", color = Color.White, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White)
            }
        }
    }
}

/**
 * High-Precision Tracing Validation:
 * Strictly verifies whether the child followed the actual shape and checkpoints of the letter.
 * Prevents random scribbles, stray marks, or drawing wrong letters from falsely passing!
 */
fun validateSmartTracing(
    userPoints: List<Offset>,
    guideDots: List<Pair<Float, Float>>,
    canvasWidth: Float,
    canvasHeight: Float,
    letter: String
): TracingValidationResult {
    // 1. Minimum points check (At least 16 recorded touch points)
    if (userPoints.size < 16 || canvasWidth <= 0f || canvasHeight <= 0f) {
        return TracingValidationResult(
            isCorrect = false,
            message = "अक्षर बहुत कम लिखा है! उंगली से पूरा अक्षर बनाएं।"
        )
    }

    // 2. Minimum stroke length check
    var totalDistance = 0f
    for (i in 0 until userPoints.size - 1) {
        totalDistance += (userPoints[i + 1] - userPoints[i]).getDistance()
    }
    val minDistance = canvasWidth * 0.35f
    if (totalDistance < minDistance) {
        return TracingValidationResult(
            isCorrect = false,
            message = "अक्षर अधूरा है! पूरी रेखाएं खींचें।"
        )
    }

    // 3. Fallback checkpoints if letter has no dots
    val checkpoints = if (guideDots.isNotEmpty()) {
        guideDots
    } else {
        listOf(
            Pair(0.50f, 0.25f),
            Pair(0.50f, 0.45f),
            Pair(0.50f, 0.65f)
        )
    }

    // 4. Checkpoint Hit Detection:
    // The child's strokes MUST pass through the key landmarks of this exact letter
    val hitThreshold = (minOf(canvasWidth, canvasHeight) * 0.15f).coerceIn(65f, 105f)

    val hitCount = checkpoints.count { (nx, ny) ->
        val target = Offset(nx * canvasWidth, ny * canvasHeight)
        userPoints.any { p -> (p - target).getDistance() <= hitThreshold }
    }

    // Must hit at least 60% of the letter's checkpoints (minimum 2)
    val requiredHits = (checkpoints.size * 0.60f).toInt().coerceAtLeast(2)
    if (hitCount < requiredHits) {
        return TracingValidationResult(
            isCorrect = false,
            message = "गलत बनावट! चमकते डॉट्स पर उंगली फेरें।"
        )
    }

    // 5. Envelope / Stray Scribble Detection:
    // The child shouldn't be scribbling wild lines outside the letter region
    val minDotX = (checkpoints.minOf { it.first } * canvasWidth - hitThreshold * 1.5f).coerceAtLeast(0f)
    val maxDotX = (checkpoints.maxOf { it.first } * canvasWidth + hitThreshold * 1.5f).coerceAtMost(canvasWidth)
    val minDotY = (checkpoints.minOf { it.second } * canvasHeight - hitThreshold * 1.5f).coerceAtLeast(0f)
    val maxDotY = (checkpoints.maxOf { it.second } * canvasHeight + hitThreshold * 1.5f).coerceAtMost(canvasHeight)

    val pointsInEnvelope = userPoints.count { p ->
        p.x in minDotX..maxDotX && p.y in minDotY..maxDotY
    }
    val envelopeRatio = pointsInEnvelope.toFloat() / userPoints.size
    if (envelopeRatio < 0.60f) {
        return TracingValidationResult(
            isCorrect = false,
            message = "स्लेट पर बाहर नहीं, केवल अक्षर पर लिखें!"
        )
    }

    return TracingValidationResult(
        isCorrect = true,
        message = "बहुत खूब! सही लिखा!"
    )
}
