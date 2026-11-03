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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidslearning.chutki.audio.HindiTtsManager
import com.kidslearning.chutki.audio.SoundEffectHelper
import com.kidslearning.chutki.data.LearningContentRepository
import com.kidslearning.chutki.data.TracingPathHelper
import com.kidslearning.chutki.model.SubjectType
import com.kidslearning.chutki.model.TracingLetter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class TracingStroke(
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float = 32f
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
                    // Small letters (a-z) with exact stroke paths
                    ('a'..'z').map { smallChar ->
                        val smallData = TracingPathHelper.getLetterTracingData(smallChar.toString(), "english")
                        TracingLetter(
                            char = smallChar.toString(),
                            subjectId = "english",
                            titleHindi = "Letter '$smallChar' लिखो",
                            hintHindi = smallData.hintHindi,
                            guideDots = smallData.guideDots,
                            audioPromptHindi = "Letter '$smallChar' लिखो!",
                            strokePaths = smallData.strokePaths
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
        filteredLetters.firstOrNull() ?: run {
            val fallbackData = TracingPathHelper.getLetterTracingData(currentLetter, activeSubject.id)
            TracingLetter(
                char = currentLetter,
                subjectId = activeSubject.id,
                titleHindi = "अक्षर '$currentLetter' लिखो",
                hintHindi = fallbackData.hintHindi,
                guideDots = fallbackData.guideDots,
                audioPromptHindi = "लिखो '$currentLetter'",
                strokePaths = fallbackData.strokePaths
            )
        }
    }

    val activeChar = currentItem.char
    val activeHint = currentItem.hintHindi.ifBlank { letterHint }

    // Stroke paths and Guide dots (Dots sit EXACTLY in the middle of each stroke line!)
    val strokePaths = remember(currentItem) {
        if (currentItem.strokePaths.isNotEmpty()) {
            currentItem.strokePaths
        } else {
            val fetched = TracingPathHelper.getLetterTracingData(activeChar, activeSubject.id)
            fetched.strokePaths
        }
    }

    val allDots = remember(currentItem, strokePaths) {
        strokePaths.flatten().distinct()
    }

    // Drawing state
    val userStrokes = remember { mutableStateListOf<TracingStroke>() }
    val currentPoints = remember { mutableStateListOf<Offset>() }
    var selectedColor by remember { mutableStateOf(Color(0xFFFFEB3B)) } // Magic yellow chalk

    // Track which dots have been touched/hit
    val hitDotIndices = remember { mutableStateListOf<Int>() }

    // Canvas dimensions
    var canvasWidth by remember { mutableFloatStateOf(0f) }
    var canvasHeight by remember { mutableFloatStateOf(0f) }

    // Feedback states
    var feedbackMessage by remember { mutableStateOf("लाइन के बीच के डॉट्स को मिलाते हुए अक्षर बनाओ!") }
    var feedbackStatus by remember { mutableStateOf<Boolean?>(null) } // null: idle, true: correct, false: wrong
    var showDemoGuide by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Demo wand animation
    val demoProgress = remember { Animatable(0f) }
    LaunchedEffect(showDemoGuide, activeChar) {
        if (showDemoGuide && allDots.size > 1) {
            while (showDemoGuide) {
                demoProgress.snapTo(0f)
                demoProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 2600, easing = LinearEasing)
                )
                delay(500)
            }
        }
    }

    // Announce letter on change and clear old strokes
    LaunchedEffect(activeChar) {
        userStrokes.clear()
        currentPoints.clear()
        hitDotIndices.clear()
        feedbackStatus = null
        feedbackMessage = "लाइन के बीच के डॉट्स को मिलाते हुए अक्षर बनाओ!"
        val prompt = when (activeSubject) {
            SubjectType.HINDI -> "जादुई स्लेट पर लिखो अक्षर '$activeChar'"
            SubjectType.ENGLISH -> "Write letter '$activeChar' on the slate"
            SubjectType.MATH -> "जादुई स्लेट पर लिखो संख्या '$activeChar'"
        }
        ttsManager?.speak(prompt)
    }

    // Chalk Color Palette
    val chalkColors = listOf(
        Color(0xFFFFEB3B), // Magic Yellow
        Color(0xFF00E676), // Neon Green
        Color(0xFF00E5FF), // Cyan Blue
        Color(0xFFFF4081), // Pink Candy
        Color(0xFFFF9100), // Bright Orange
        Color(0xFFFFFFFF)  // Chalk White
    )

    // Helper: Checks touch point or segment against allDots
    fun checkPointHit(px: Float, py: Float, radius: Float) {
        allDots.forEachIndexed { idx, (nx, ny) ->
            if (!hitDotIndices.contains(idx)) {
                val dx = px - nx * canvasWidth
                val dy = py - ny * canvasHeight
                if (dx * dx + dy * dy <= radius * radius) {
                    hitDotIndices.add(idx)
                    SoundEffectHelper.playPopSound()
                }
            }
        }
    }

    fun checkSegmentHit(a: Offset, b: Offset, radius: Float) {
        allDots.forEachIndexed { idx, (nx, ny) ->
            if (!hitDotIndices.contains(idx)) {
                val dotTarget = Offset(nx * canvasWidth, ny * canvasHeight)
                val dist = distanceToSegment(dotTarget, a, b)
                if (dist <= radius) {
                    hitDotIndices.add(idx)
                    SoundEffectHelper.playPopSound()
                }
            }
        }
    }

    fun checkSuccessCondition() {
        val requiredCount = (allDots.size * 0.70f).toInt().coerceAtLeast(minOf(2, allDots.size))
        if (hitDotIndices.size >= requiredCount && feedbackStatus != true) {
            feedbackStatus = true
            feedbackMessage = "शाबाश! बिल्कुल सही लिखा! 🎉"
            showDemoGuide = false
            SoundEffectHelper.playCorrectChime()
            ttsManager?.speak("शाबाश! बिल्कुल सही लिखा!")
            onLetterTraced?.invoke(activeChar)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF11141E)) // Deep dark slate background
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
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${if (activeSubject == SubjectType.MATH) "संख्या" else "अक्षर"} '$activeChar' (${letterIndex + 1}/${filteredLetters.size})",
                    color = Color(0xFFFFB300),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Eraser button
            IconButton(
                onClick = {
                    SoundEffectHelper.playPopSound()
                    userStrokes.clear()
                    currentPoints.clear()
                    hitDotIndices.clear()
                    feedbackStatus = null
                    feedbackMessage = "लाइन के बीच के डॉट्स को मिलाते हुए अक्षर बनाओ!"
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
                            hitDotIndices.clear()
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
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2032)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFF3F51B5))
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = "“$activeHint”",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            showDemoGuide = !showDemoGuide
                            if (showDemoGuide) {
                                ttsManager?.speak("जादू देखो! लाइन के बीच में चमकते डॉट्स को मिलाओ!")
                            }
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (showDemoGuide) "🪄 गाइड छिपाएं" else "🪄 जादू देखो (कैसे लिखें?)",
                            color = Color(0xFF80D8FF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Status Indicator
                    if (feedbackStatus != null) {
                        Text(
                            text = if (feedbackStatus == true) "✅ शाबाश! बहुत अच्छा" else "❌ गलत बनावट! फिर लिखें",
                            color = if (feedbackStatus == true) Color(0xFF00E676) else Color(0xFFFF5252),
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    } else {
                        // Dot progress counter
                        Text(
                            text = "डॉट्स: ${hitDotIndices.size}/${allDots.size}",
                            color = Color(0xFFFFD54F),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- 4. Black Slate Canvas: Tracing Line + Dots Right in the Middle ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF0B0E14))
                .border(3.dp, Color(0xFFFF9800), RoundedCornerShape(24.dp))
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(activeChar) {
                        val hitRadius = 38.dp.toPx()
                        detectDragGestures(
                            onDragStart = { offset ->
                                currentPoints.clear()
                                currentPoints.add(offset)
                                checkPointHit(offset.x, offset.y, hitRadius)
                                checkSuccessCondition()
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                val prev = currentPoints.lastOrNull()
                                currentPoints.add(change.position)
                                if (prev != null) {
                                    checkSegmentHit(prev, change.position, hitRadius)
                                } else {
                                    checkPointHit(change.position.x, change.position.y, hitRadius)
                                }
                                checkSuccessCondition()
                            },
                            onDragEnd = {
                                if (currentPoints.isNotEmpty()) {
                                    userStrokes.add(
                                        TracingStroke(
                                            points = currentPoints.toList(),
                                            color = selectedColor,
                                            strokeWidth = 32f
                                        )
                                    )
                                    currentPoints.clear()
                                    checkSuccessCondition()
                                }
                            },
                            onDragCancel = {
                                currentPoints.clear()
                            }
                        )
                    }
            ) {
                canvasWidth = size.width
                canvasHeight = size.height
                val w = size.width
                val h = size.height

                // 4A. Traditional Notebook / Slate Guide Lines
                val guideLineColor = Color(0x18FFFFFF)
                drawLine(guideLineColor, Offset(24f, h * 0.18f), Offset(w - 24f, h * 0.18f), strokeWidth = 2f)
                drawLine(guideLineColor, Offset(24f, h * 0.48f), Offset(w - 24f, h * 0.48f), strokeWidth = 2f)
                drawLine(guideLineColor, Offset(24f, h * 0.78f), Offset(w - 24f, h * 0.78f), strokeWidth = 2f)

                // 4B. Soft Watermark Character in Background (Subtle outline silhouette)
                val watermarkPaint = Paint().apply {
                    textSize = if (activeChar.length > 2) h * 0.42f else if (activeChar.length > 1) h * 0.50f else h * 0.58f
                    color = android.graphics.Color.argb(20, 255, 255, 255)
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                    isFakeBoldText = true
                }
                drawContext.canvas.nativeCanvas.drawText(
                    activeChar,
                    w / 2,
                    h * 0.65f,
                    watermarkPaint
                )

                // 4C. TRACING LINES (The exact road/track for the child to trace)
                strokePaths.forEach { strokePoints ->
                    if (strokePoints.size >= 2) {
                        val path = Path().apply {
                            strokePoints.forEachIndexed { i, (nx, ny) ->
                                val px = nx * w
                                val py = ny * h
                                if (i == 0) moveTo(px, py) else lineTo(px, py)
                            }
                        }

                        // Wide translucent chalk road (lane)
                        drawPath(
                            path = path,
                            color = Color(0x28FFFFFF),
                            style = Stroke(
                                width = 30.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )

                        // Clear dashed tracing centerline
                        drawPath(
                            path = path,
                            color = Color(0xB380D8FF), // Cyan chalk dashed tracing line
                            style = Stroke(
                                width = 4.5.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                            )
                        )
                    }
                }

                // 4D. GUIDE DOTS - Placed EXACTLY in the middle of the tracing lines!
                val dotNumberPaint = Paint().apply {
                    textSize = 10.dp.toPx()
                    color = android.graphics.Color.WHITE
                    textAlign = Paint.Align.CENTER
                    isFakeBoldText = true
                    isAntiAlias = true
                }

                allDots.forEachIndexed { dotIndex, (nx, ny) ->
                    val cx = nx * w
                    val cy = ny * h
                    val isHit = hitDotIndices.contains(dotIndex)

                    // Outer halo
                    drawCircle(
                        color = if (isHit) Color(0x5500E676) else Color(0x33FFD54F),
                        radius = if (isHit) 18.dp.toPx() else 14.dp.toPx(),
                        center = Offset(cx, cy)
                    )

                    // Core dot
                    drawCircle(
                        color = if (isHit) Color(0xFF00E676) else Color(0xFFFFB300),
                        radius = if (isHit) 13.dp.toPx() else 11.dp.toPx(),
                        center = Offset(cx, cy)
                    )

                    // Inside: Step number (1, 2, 3...) or white checkmark if completed!
                    if (isHit) {
                        drawCircle(
                            color = Color.White,
                            radius = 4.dp.toPx(),
                            center = Offset(cx, cy)
                        )
                    } else {
                        val label = (dotIndex + 1).toString()
                        drawContext.canvas.nativeCanvas.drawText(
                            label,
                            cx,
                            cy + 3.5.dp.toPx(),
                            dotNumberPaint
                        )
                    }
                }

                // 4E. User Completed Chalk Strokes
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

                // 4F. Current Live Chalk Stroke Under Finger
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
                            width = 32f,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }

                // 4G. Animated Magic Wand ("🪄 जादू देखो") - Smoothly follows each stroke in order!
                if (showDemoGuide && allDots.size > 1) {
                    val totalSegments = allDots.size - 1
                    val currentStep = (demoProgress.value * totalSegments).toInt().coerceIn(0, totalSegments - 1)
                    val frac = (demoProgress.value * totalSegments) - currentStep
                    val (x1, y1) = allDots[currentStep]
                    val (x2, y2) = allDots[(currentStep + 1).coerceAtMost(allDots.size - 1)]
                    val wandX = (x1 + (x2 - x1) * frac) * w
                    val wandY = (y1 + (y2 - y1) * frac) * h

                    // Glowing magic wand star
                    drawCircle(
                        color = Color(0x66FFEB3B),
                        radius = 22.dp.toPx(),
                        center = Offset(wandX, wandY)
                    )
                    drawCircle(
                        color = Color(0xFFFFD54F),
                        radius = 13.dp.toPx(),
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
                    text = "👉 लाइन के बीच में डॉट्स 1, 2, 3... को मिलाते हुए लिखें",
                    color = Color(0xFFFFEB3B),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 10.dp)
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
                        guideDots = allDots,
                        hitDotCount = hitDotIndices.size,
                        canvasWidth = canvasWidth,
                        canvasHeight = canvasHeight
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
 * Calculates perpendicular distance from point p to line segment (a, b).
 * Enables 100% reliable touch detection even when the child moves finger quickly!
 */
fun distanceToSegment(p: Offset, a: Offset, b: Offset): Float {
    val dx = b.x - a.x
    val dy = b.y - a.y
    val l2 = dx * dx + dy * dy
    if (l2 == 0f) return (p - a).getDistance()
    val t = (((p.x - a.x) * dx + (p.y - a.y) * dy) / l2).coerceIn(0f, 1f)
    val proj = Offset(a.x + t * dx, a.y + t * dy)
    return (p - proj).getDistance()
}

/**
 * Validates the child's chalk drawing:
 * Checks dot checkpoints, minimum stroke effort, and stray mark prevention.
 */
fun validateSmartTracing(
    userPoints: List<Offset>,
    guideDots: List<Pair<Float, Float>>,
    hitDotCount: Int,
    canvasWidth: Float,
    canvasHeight: Float
): TracingValidationResult {
    if (userPoints.size < 12 || canvasWidth <= 0f || canvasHeight <= 0f) {
        return TracingValidationResult(
            isCorrect = false,
            message = "अक्षर बहुत कम लिखा है! उंगली से डॉट्स को मिलाओ।"
        )
    }

    val requiredHits = (guideDots.size * 0.65f).toInt().coerceAtLeast(minOf(2, guideDots.size))
    if (hitDotCount < requiredHits) {
        return TracingValidationResult(
            isCorrect = false,
            message = "लाइन के बीच वाले चमकते डॉट्स पर उंगली फेरें!"
        )
    }

    // Stray scribbling detection: check bounds envelope
    val minX = (guideDots.minOf { it.first } * canvasWidth - 120f).coerceAtLeast(0f)
    val maxX = (guideDots.maxOf { it.first } * canvasWidth + 120f).coerceAtMost(canvasWidth)
    val minY = (guideDots.minOf { it.second } * canvasHeight - 120f).coerceAtLeast(0f)
    val maxY = (guideDots.maxOf { it.second } * canvasHeight + 120f).coerceAtMost(canvasHeight)

    val insideCount = userPoints.count { p -> p.x in minX..maxX && p.y in minY..maxY }
    val ratio = insideCount.toFloat() / userPoints.size
    if (ratio < 0.60f) {
        return TracingValidationResult(
            isCorrect = false,
            message = "स्लेट पर बाहर नहीं, केवल अक्षर की लाइन पर लिखें!"
        )
    }

    return TracingValidationResult(
        isCorrect = true,
        message = "शाबाश! बहुत अच्छा लिखा!"
    )
}
