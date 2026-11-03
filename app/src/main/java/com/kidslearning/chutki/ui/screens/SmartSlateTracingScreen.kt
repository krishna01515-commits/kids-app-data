package com.kidslearning.chutki.ui.screens

import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.PathMeasure
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

enum class SlateDrawingMode {
    TRACING_LINE, // ✍️ ट्रेसिंग लाइन (Smooth road + path proximity check)
    CONNECT_DOTS  // 🟢 डॉट्स मिलाओ (Connect numbered dots puzzle)
}

data class TracingStroke(
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float = 34f
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

    // 3. Active Mode: Tracing Line vs Connect Dots (Separate independent options!)
    var slateMode by remember { mutableStateOf(SlateDrawingMode.TRACING_LINE) }

    // 4. Sub-category tabs
    var selectedSubTabIndex by remember(activeSubject.id) { mutableIntStateOf(0) }

    val subTabTitles = when (activeSubject) {
        SubjectType.HINDI -> listOf("सभी (अ-ज्ञ)", "स्वर (अ-अः)", "व्यंजन (क-ज्ञ)")
        SubjectType.ENGLISH -> listOf("Capital (A-Z)", "Small (a-z)")
        SubjectType.MATH -> listOf("1 से 20", "21 से 50", "51 से 100")
    }

    // 5. Filter letters strictly based on subject and selected sub-tab
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

    // 6. Letter navigation index
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

    // Stroke paths for active character
    val strokePaths = remember(currentItem) {
        if (currentItem.strokePaths.isNotEmpty()) {
            currentItem.strokePaths
        } else {
            val fetched = TracingPathHelper.getLetterTracingData(activeChar, activeSubject.id)
            fetched.strokePaths
        }
    }

    // Guide nodes for Tracing Line mode: Only 2-3 main nodes (Start 🟢, Mid 🟡, End 🏁)
    val guideNodes = remember(strokePaths) {
        TracingPathHelper.extractGuideNodes(strokePaths)
    }

    // Well-spaced dots for Connect Dots mode (filtered to prevent overlapping bubbles!)
    val wellSpacedDots = remember(currentItem, strokePaths) {
        val rawDots = currentItem.guideDots.ifEmpty { strokePaths.flatten() }
        if (rawDots.isEmpty()) emptyList()
        else {
            val result = mutableListOf<Pair<Float, Float>>()
            val minDistance = 0.12f // Clean spacing so number bubbles never collide
            rawDots.forEach { pt ->
                val isTooClose = result.any { existing ->
                    val dx = existing.first - pt.first
                    val dy = existing.second - pt.second
                    kotlin.math.hypot(dx, dy) < minDistance
                }
                if (!isTooClose) {
                    result.add(pt)
                }
            }
            if (rawDots.size >= 2 && !result.contains(rawDots.last())) {
                result.add(rawDots.last())
            }
            result
        }
    }

    // Drawing state
    val userStrokes = remember { mutableStateListOf<TracingStroke>() }
    val currentPoints = remember { mutableStateListOf<Offset>() }
    var selectedColor by remember { mutableStateOf(Color(0xFFFFEB3B)) } // Magic yellow chalk

    // Mode 1 Progress: Covered sample indices
    val coveredSampleIndices = remember { mutableStateListOf<Int>() }

    // Mode 2 Progress: Hit dot indices
    val hitDotIndices = remember { mutableStateListOf<Int>() }

    // Canvas dimensions
    var canvasWidth by remember { mutableFloatStateOf(0f) }
    var canvasHeight by remember { mutableFloatStateOf(0f) }

    // Feedback states
    var feedbackMessage by remember(slateMode) {
        mutableStateOf(
            if (slateMode == SlateDrawingMode.TRACING_LINE) "चमकती लाइन पर उंगली फेरकर 80% भरें!"
            else "नंबर 1 से शुरू करके डॉट्स मिलाओ!"
        )
    }
    var feedbackStatus by remember { mutableStateOf<Boolean?>(null) }
    var showDemoGuide by remember { mutableStateOf(false) }

    // Demo wand animation
    val demoProgress = remember { Animatable(0f) }
    LaunchedEffect(showDemoGuide, activeChar, slateMode) {
        if (showDemoGuide) {
            while (showDemoGuide) {
                demoProgress.snapTo(0f)
                demoProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 3200, easing = LinearEasing)
                )
                delay(400)
            }
        }
    }

    // Announce letter on change and clear old strokes
    LaunchedEffect(activeChar) {
        userStrokes.clear()
        currentPoints.clear()
        coveredSampleIndices.clear()
        hitDotIndices.clear()
        feedbackStatus = null
        feedbackMessage = if (slateMode == SlateDrawingMode.TRACING_LINE) "चमकती लाइन पर उंगली फेरकर 80% भरें!" else "नंबर 1 से शुरू करके डॉट्स मिलाओ!"
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

    // Mode 1: Calculate dense sample checkpoints along smooth Bézier curves
    val pathSampleCheckpoints = remember(strokePaths, canvasWidth, canvasHeight) {
        if (canvasWidth <= 0f || canvasHeight <= 0f || strokePaths.isEmpty()) {
            emptyList()
        } else {
            val samples = mutableListOf<Offset>()
            strokePaths.forEach { stroke ->
                if (stroke.size >= 2) {
                    val smoothPath = buildSmoothComposePath(stroke, canvasWidth, canvasHeight)
                    val androidPath = smoothPath.asAndroidPath()
                    val pm = PathMeasure(androidPath, false)
                    val len = pm.length
                    if (len > 0f) {
                        val numSteps = (len / 16f).toInt().coerceIn(12, 50)
                        val pos = FloatArray(2)
                        for (s in 0..numSteps) {
                            val dist = (s.toFloat() / numSteps) * len
                            pm.getPosTan(dist, pos, null)
                            samples.add(Offset(pos[0], pos[1]))
                        }
                    }
                } else if (stroke.size == 1) {
                    samples.add(Offset(stroke[0].first * canvasWidth, stroke[0].second * canvasHeight))
                }
            }
            samples
        }
    }

    val totalSamples = pathSampleCheckpoints.size.coerceAtLeast(1)
    val coverageRatio = (coveredSampleIndices.size.toFloat() / totalSamples).coerceIn(0f, 1f)

    // Mode 1 Helper: Proximity check for Tracing Line
    fun checkProximitySegment(a: Offset, b: Offset, radius: Float) {
        pathSampleCheckpoints.forEachIndexed { idx, samplePt ->
            if (!coveredSampleIndices.contains(idx)) {
                val dist = distanceToSegment(samplePt, a, b)
                if (dist <= radius) {
                    coveredSampleIndices.add(idx)
                }
            }
        }
    }

    fun checkProximityPoint(pt: Offset, radius: Float) {
        pathSampleCheckpoints.forEachIndexed { idx, samplePt ->
            if (!coveredSampleIndices.contains(idx)) {
                val dx = pt.x - samplePt.x
                val dy = pt.y - samplePt.y
                if (dx * dx + dy * dy <= radius * radius) {
                    coveredSampleIndices.add(idx)
                }
            }
        }
    }

    fun checkCoverageSuccess() {
        val currentRatio = (coveredSampleIndices.size.toFloat() / totalSamples)
        if (currentRatio >= 0.80f && feedbackStatus != true) {
            feedbackStatus = true
            feedbackMessage = "शाबाश! बिल्कुल सही लिखा! 🎉"
            showDemoGuide = false
            SoundEffectHelper.playCorrectChime()
            ttsManager?.speak("शाबाश! बिल्कुल सही लिखा!")
            onLetterTraced?.invoke(activeChar)
        }
    }

    // Mode 2 Helper: Dot detection for Connect Dots
    fun checkDotHitPoint(pt: Offset, radius: Float) {
        wellSpacedDots.forEachIndexed { idx, (nx, ny) ->
            if (!hitDotIndices.contains(idx)) {
                val dx = pt.x - nx * canvasWidth
                val dy = pt.y - ny * canvasHeight
                if (dx * dx + dy * dy <= radius * radius) {
                    hitDotIndices.add(idx)
                    SoundEffectHelper.playPopSound()
                }
            }
        }
    }

    fun checkDotHitSegment(a: Offset, b: Offset, radius: Float) {
        wellSpacedDots.forEachIndexed { idx, (nx, ny) ->
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

    fun checkDotsSuccess() {
        val requiredCount = (wellSpacedDots.size * 0.80f).toInt().coerceAtLeast(minOf(2, wellSpacedDots.size))
        if (hitDotIndices.size >= requiredCount && feedbackStatus != true) {
            feedbackStatus = true
            feedbackMessage = "शाबाश! सारे डॉट्स मिला दिए! 🎉"
            showDemoGuide = false
            SoundEffectHelper.playCorrectChime()
            ttsManager?.speak("शाबाश! सारे डॉट्स मिला दिए!")
            onLetterTraced?.invoke(activeChar)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F131D)) // Deep dark obsidian slate
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
                    coveredSampleIndices.clear()
                    hitDotIndices.clear()
                    feedbackStatus = null
                    feedbackMessage = if (slateMode == SlateDrawingMode.TRACING_LINE) "चमकती लाइन पर उंगली फेरकर 80% भरें!" else "नंबर 1 से शुरू करके डॉट्स मिलाओ!"
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFFFF5252))
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Clear", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // --- 2. Independent Mode Selector Switcher (ट्रेसिंग लाइन vs डॉट्स मिलाओ) ---
        // Separates both options completely so only one mode runs at a time with zero overlap!
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF192033))
                .padding(3.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            val isTracing = slateMode == SlateDrawingMode.TRACING_LINE
            Button(
                onClick = {
                    if (slateMode != SlateDrawingMode.TRACING_LINE) {
                        slateMode = SlateDrawingMode.TRACING_LINE
                        userStrokes.clear()
                        currentPoints.clear()
                        coveredSampleIndices.clear()
                        feedbackStatus = null
                        feedbackMessage = "चमकती लाइन पर उंगली फेरकर 80% भरें!"
                        ttsManager?.speak("ट्रेसिंग लाइन पर उंगली फेरकर लिखो!")
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isTracing) Color(0xFFFF9800) else Color.Transparent
                ),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "✍️ ट्रेसिंग लाइन",
                    color = if (isTracing) Color.Black else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            val isDots = slateMode == SlateDrawingMode.CONNECT_DOTS
            Button(
                onClick = {
                    if (slateMode != SlateDrawingMode.CONNECT_DOTS) {
                        slateMode = SlateDrawingMode.CONNECT_DOTS
                        userStrokes.clear()
                        currentPoints.clear()
                        hitDotIndices.clear()
                        feedbackStatus = null
                        feedbackMessage = "नंबर 1 से शुरू करके डॉट्स मिलाओ!"
                        ttsManager?.speak("डॉट्स 1, 2, 3 को मिलाओ!")
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDots) Color(0xFF00E676) else Color.Transparent
                ),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "🟢 डॉट्स मिलाओ",
                    color = if (isDots) Color.Black else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // --- 3. Subject-Specific Category Tabs ---
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
                            coveredSampleIndices.clear()
                            hitDotIndices.clear()
                            feedbackStatus = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) Color(0xFF673AB7) else Color(0xFF23293D)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 5.dp)
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

        Spacer(modifier = Modifier.height(6.dp))

        // --- 4. Priya Didi Guidance & Progress Box ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF181E30)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFF3F51B5))
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = "“$activeHint”",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Demo Wand Button
                    TextButton(
                        onClick = {
                            showDemoGuide = !showDemoGuide
                            if (showDemoGuide) {
                                val wandPrompt = if (slateMode == SlateDrawingMode.TRACING_LINE)
                                    "जादू देखो! चमकते सितारे के साथ-साथ उंगली फेरें!"
                                else "जादू देखो! डॉट्स 1, 2, 3 को क्रम से मिलाओ!"
                                ttsManager?.speak(wandPrompt)
                            }
                        },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (showDemoGuide) "🪄 गाइड छिपाएं" else "🪄 जादू देखो (रास्ता समझें)",
                            color = Color(0xFF80D8FF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Mode-Specific Live Progress Indicator
                    if (slateMode == SlateDrawingMode.TRACING_LINE) {
                        val percent = (coverageRatio * 100).toInt()
                        val isPass = coverageRatio >= 0.80f
                        Surface(
                            color = if (isPass) Color(0xFF00E676) else Color(0x33FFB300),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (isPass) "✅ $percent% पूरा! शाबाश!" else "⭐ $percent% / 80%",
                                color = if (isPass) Color.Black else Color(0xFFFFD54F),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    } else {
                        // Connect Dots Mode Counter
                        val isAllDots = hitDotIndices.size >= wellSpacedDots.size && wellSpacedDots.isNotEmpty()
                        Surface(
                            color = if (isAllDots) Color(0xFF00E676) else Color(0x3300E676),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (isAllDots) "✅ सारे डॉट्स मिल गए!" else "🟢 डॉट्स: ${hitDotIndices.size}/${wellSpacedDots.size}",
                                color = if (isAllDots) Color.Black else Color(0xFF00E676),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                // Mini Progress Bar for Tracing Line mode
                if (slateMode == SlateDrawingMode.TRACING_LINE) {
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { (coverageRatio / 0.80f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (coverageRatio >= 0.80f) Color(0xFF00E676) else Color(0xFF00E5FF),
                        trackColor = Color(0x22FFFFFF)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- 5. Black Slate Canvas: Dedicated Single Mode Display (No Override!) ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF090C12))
                .border(3.dp, Color(0xFFFF9800), RoundedCornerShape(24.dp))
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(activeChar, slateMode) {
                        val touchRadius = 38.dp.toPx()
                        detectDragGestures(
                            onDragStart = { offset ->
                                currentPoints.clear()
                                currentPoints.add(offset)
                                if (slateMode == SlateDrawingMode.TRACING_LINE) {
                                    checkProximityPoint(offset, touchRadius)
                                    checkCoverageSuccess()
                                } else {
                                    checkDotHitPoint(offset, touchRadius)
                                    checkDotsSuccess()
                                }
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                val prev = currentPoints.lastOrNull()
                                currentPoints.add(change.position)
                                if (slateMode == SlateDrawingMode.TRACING_LINE) {
                                    if (prev != null) {
                                        checkProximitySegment(prev, change.position, touchRadius)
                                    } else {
                                        checkProximityPoint(change.position, touchRadius)
                                    }
                                    checkCoverageSuccess()
                                } else {
                                    if (prev != null) {
                                        checkDotHitSegment(prev, change.position, touchRadius)
                                    } else {
                                        checkDotHitPoint(change.position, touchRadius)
                                    }
                                    checkDotsSuccess()
                                }
                            },
                            onDragEnd = {
                                if (currentPoints.isNotEmpty()) {
                                    userStrokes.add(
                                        TracingStroke(
                                            points = currentPoints.toList(),
                                            color = selectedColor,
                                            strokeWidth = 34f
                                        )
                                    )
                                    currentPoints.clear()
                                    if (slateMode == SlateDrawingMode.TRACING_LINE) {
                                        checkCoverageSuccess()
                                    } else {
                                        checkDotsSuccess()
                                    }
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

                // Common: Traditional Slate Guide Lines
                val guideLineColor = Color(0x18FFFFFF)
                drawLine(guideLineColor, Offset(24f, h * 0.18f), Offset(w - 24f, h * 0.18f), strokeWidth = 2f)
                drawLine(guideLineColor, Offset(24f, h * 0.48f), Offset(w - 24f, h * 0.48f), strokeWidth = 2f)
                drawLine(guideLineColor, Offset(24f, h * 0.78f), Offset(w - 24f, h * 0.78f), strokeWidth = 2f)

                // =========================================================================
                // MODE 1: TRACING LINE (✍️ Smooth Road + Path Proximity + Magic Star)
                // NO numbered dots shown here! Clean and focused on letter curves.
                // =========================================================================
                if (slateMode == SlateDrawingMode.TRACING_LINE) {
                    // 1A. Actual Letter Native Font Typography (True Curves)
                    val fontSize = if (activeChar.length > 2) h * 0.38f else if (activeChar.length > 1) h * 0.46f else h * 0.54f
                    val fontTextPaint = Paint().apply {
                        textSize = fontSize
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                        textAlign = Paint.Align.CENTER
                        isAntiAlias = true
                    }
                    val textBounds = android.graphics.Rect()
                    fontTextPaint.getTextBounds(activeChar, 0, activeChar.length, textBounds)
                    val textCenterX = w / 2f
                    val textCenterY = (h / 2f) - textBounds.exactCenterY()

                    val fontLetterPath = android.graphics.Path()
                    fontTextPaint.getTextPath(activeChar, 0, activeChar.length, textCenterX, textCenterY, fontLetterPath)

                    // Soft chalk silhouette fill
                    val fillPaint = Paint().apply {
                        color = android.graphics.Color.argb(22, 255, 255, 255)
                        style = Paint.Style.FILL
                        isAntiAlias = true
                    }
                    drawContext.canvas.nativeCanvas.drawPath(fontLetterPath, fillPaint)

                    // Dashed typographical contour
                    val dashedContourPaint = Paint().apply {
                        color = android.graphics.Color.argb(90, 128, 216, 255)
                        style = Paint.Style.STROKE
                        strokeWidth = 3.dp.toPx()
                        pathEffect = DashPathEffect(floatArrayOf(14f, 14f), 0f)
                        isAntiAlias = true
                    }
                    drawContext.canvas.nativeCanvas.drawPath(fontLetterPath, dashedContourPaint)

                    // 1B. Smooth Catmull-Rom Bézier center-paths
                    strokePaths.forEach { stroke ->
                        if (stroke.size >= 2) {
                            val smoothPath = buildSmoothComposePath(stroke, w, h)

                            // Wide chalk road
                            drawPath(
                                path = smoothPath,
                                color = Color(0x24FFFFFF),
                                style = Stroke(
                                    width = 30.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )

                            // Dotted Centerline Guide
                            drawPath(
                                path = smoothPath,
                                color = Color(0x9980D8FF),
                                style = Stroke(
                                    width = 4.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                                )
                            )
                        }
                    }

                    // 1C. Neon chalk highlight over covered path sections
                    coveredSampleIndices.forEach { sIdx ->
                        if (sIdx in pathSampleCheckpoints.indices) {
                            val pt = pathSampleCheckpoints[sIdx]
                            drawCircle(
                                color = Color(0x3300E676),
                                radius = 16.dp.toPx(),
                                center = pt
                            )
                            drawCircle(
                                color = Color(0xFF00E676),
                                radius = 6.dp.toPx(),
                                center = pt
                            )
                        }
                    }

                    // 1D. Clean Start 🟢 & End 🏁 Nodes only (NO numbered bubbles!)
                    guideNodes.forEach { node ->
                        val cx = node.x * w
                        val cy = node.y * h
                        when (node.type) {
                            TracingPathHelper.GuideNodeType.START -> {
                                drawCircle(Color(0x5500E676), 17.dp.toPx(), Offset(cx, cy))
                                drawCircle(Color(0xFF00E676), 12.dp.toPx(), Offset(cx, cy))
                                val textPaint = Paint().apply {
                                    textSize = 10.dp.toPx()
                                    color = android.graphics.Color.WHITE
                                    textAlign = Paint.Align.CENTER
                                    isFakeBoldText = true
                                    isAntiAlias = true
                                }
                                drawContext.canvas.nativeCanvas.drawText(node.label, cx, cy + 3.5.dp.toPx(), textPaint)
                            }
                            TracingPathHelper.GuideNodeType.MID -> {
                                drawCircle(Color(0x33FFB300), 10.dp.toPx(), Offset(cx, cy))
                                drawCircle(Color(0xFFFFB300), 7.dp.toPx(), Offset(cx, cy))
                            }
                            TracingPathHelper.GuideNodeType.END -> {
                                drawCircle(Color(0x55FF5252), 17.dp.toPx(), Offset(cx, cy))
                                drawCircle(Color(0xFFFF5252), 12.dp.toPx(), Offset(cx, cy))
                                val flagPaint = Paint().apply {
                                    textSize = 10.dp.toPx()
                                    color = android.graphics.Color.WHITE
                                    textAlign = Paint.Align.CENTER
                                    isFakeBoldText = true
                                    isAntiAlias = true
                                }
                                drawContext.canvas.nativeCanvas.drawText(node.label, cx, cy + 3.5.dp.toPx(), flagPaint)
                            }
                        }
                    }

                    // 1E. Animated Magic Star gliding along Bézier curves
                    if (showDemoGuide && pathSampleCheckpoints.isNotEmpty()) {
                        val totalSteps = pathSampleCheckpoints.size - 1
                        val currentStep = (demoProgress.value * totalSteps).toInt().coerceIn(0, totalSteps - 1)
                        val frac = (demoProgress.value * totalSteps) - currentStep
                        val p1 = pathSampleCheckpoints[currentStep]
                        val p2 = pathSampleCheckpoints[(currentStep + 1).coerceAtMost(pathSampleCheckpoints.size - 1)]
                        val starX = p1.x + (p2.x - p1.x) * frac
                        val starY = p1.y + (p2.y - p1.y) * frac

                        drawCircle(Color(0x66FFEB3B), 22.dp.toPx(), Offset(starX, starY))
                        drawCircle(Color(0xFFFFD54F), 13.dp.toPx(), Offset(starX, starY))
                        drawCircle(Color.White, 6.dp.toPx(), Offset(starX, starY))
                    }
                }

                // =========================================================================
                // MODE 2: CONNECT DOTS (🟢 Clean Numbered Dots Puzzle)
                // NO thick font track here! Only clear, well-spaced numbered dots to connect!
                // =========================================================================
                if (slateMode == SlateDrawingMode.CONNECT_DOTS) {
                    // 2A. Faint connecting dashed guidelines between consecutive dots
                    if (wellSpacedDots.size >= 2) {
                        for (i in 0 until wellSpacedDots.size - 1) {
                            val p1 = Offset(wellSpacedDots[i].first * w, wellSpacedDots[i].second * h)
                            val p2 = Offset(wellSpacedDots[i + 1].first * w, wellSpacedDots[i + 1].second * h)
                            drawLine(
                                color = Color(0x3380D8FF),
                                start = p1,
                                end = p2,
                                strokeWidth = 3.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            )
                        }
                    }

                    // 2B. Clear, well-spaced, numbered golden/green dots (Zero overlap!)
                    val dotPaint = Paint().apply {
                        textSize = 12.dp.toPx()
                        color = android.graphics.Color.WHITE
                        textAlign = Paint.Align.CENTER
                        isFakeBoldText = true
                        isAntiAlias = true
                    }

                    wellSpacedDots.forEachIndexed { dotIndex, (nx, ny) ->
                        val cx = nx * w
                        val cy = ny * h
                        val isHit = hitDotIndices.contains(dotIndex)

                        // Outer ring glow
                        drawCircle(
                            color = if (isHit) Color(0x5500E676) else Color(0x33FFB300),
                            radius = if (isHit) 19.dp.toPx() else 16.dp.toPx(),
                            center = Offset(cx, cy)
                        )

                        // Core circle
                        drawCircle(
                            color = if (isHit) Color(0xFF00E676) else Color(0xFFFFB300),
                            radius = if (isHit) 14.dp.toPx() else 12.dp.toPx(),
                            center = Offset(cx, cy)
                        )

                        // Inside label: Number (1, 2, 3...) or checkmark if hit!
                        if (isHit) {
                            drawCircle(Color.White, 4.dp.toPx(), Offset(cx, cy))
                        } else {
                            val label = (dotIndex + 1).toString()
                            drawContext.canvas.nativeCanvas.drawText(
                                label,
                                cx,
                                cy + 4.dp.toPx(),
                                dotPaint
                            )
                        }
                    }

                    // 2C. Animated Magic Star showing dot sequence 1 -> 2 -> 3
                    if (showDemoGuide && wellSpacedDots.size >= 2) {
                        val totalSegments = wellSpacedDots.size - 1
                        val currentStep = (demoProgress.value * totalSegments).toInt().coerceIn(0, totalSegments - 1)
                        val frac = (demoProgress.value * totalSegments) - currentStep
                        val (x1, y1) = wellSpacedDots[currentStep]
                        val (x2, y2) = wellSpacedDots[(currentStep + 1).coerceAtMost(wellSpacedDots.size - 1)]
                        val starX = (x1 + (x2 - x1) * frac) * w
                        val starY = (y1 + (y2 - y1) * frac) * h

                        drawCircle(Color(0x66FFEB3B), 22.dp.toPx(), Offset(starX, starY))
                        drawCircle(Color(0xFFFFD54F), 13.dp.toPx(), Offset(starX, starY))
                        drawCircle(Color.White, 6.dp.toPx(), Offset(starX, starY))
                    }
                }

                // Common: User Completed Chalk Strokes
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

                // Common: Current Live Chalk Stroke Under Finger
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
                            width = 34f,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }

            // Top Guidance Pill
            if (showDemoGuide) {
                val demoText = if (slateMode == SlateDrawingMode.TRACING_LINE)
                    "👉 चमकते सितारे के साथ 🟢 से 🏁 तक लिखें"
                else "👉 चमकते सितारे के साथ डॉट्स 1, 2, 3... को मिलाएं"
                Text(
                    text = demoText,
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

        // --- 6. Chalk Color Palette & Smart Auto-Check Button ---
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
                    val validation = if (slateMode == SlateDrawingMode.TRACING_LINE) {
                        validateSmartPathTracing(
                            userPoints = allPoints,
                            coverageRatio = coverageRatio,
                            canvasWidth = canvasWidth,
                            canvasHeight = canvasHeight
                        )
                    } else {
                        validateConnectDotsTracing(
                            userPoints = allPoints,
                            hitDotCount = hitDotIndices.size,
                            totalDots = wellSpacedDots.size,
                            canvasWidth = canvasWidth,
                            canvasHeight = canvasHeight
                        )
                    }

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

        // --- 7. Navigation Buttons ---
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
 * Builds a natural, silky-smooth continuous Bézier curve (Catmull-Rom spline)
 * through a list of stroke waypoints. Removes all polygonal jagged straight lines!
 */
fun buildSmoothComposePath(points: List<Pair<Float, Float>>, w: Float, h: Float): Path {
    val path = Path()
    if (points.isEmpty()) return path
    val pts = points.map { Offset(it.first * w, it.second * h) }
    path.moveTo(pts[0].x, pts[0].y)
    if (pts.size == 1) return path
    if (pts.size == 2) {
        path.lineTo(pts[1].x, pts[1].y)
        return path
    }

    // Catmull-Rom to Cubic Bézier spline interpolation
    for (i in 0 until pts.size - 1) {
        val p0 = if (i > 0) pts[i - 1] else pts[i]
        val p1 = pts[i]
        val p2 = pts[i + 1]
        val p3 = if (i < pts.size - 2) pts[i + 2] else pts[i + 1]

        val c1x = p1.x + (p2.x - p0.x) / 6f
        val c1y = p1.y + (p2.y - p0.y) / 6f
        val c2x = p2.x - (p3.x - p1.x) / 6f
        val c2y = p2.y - (p3.y - p1.y) / 6f

        path.cubicTo(c1x, c1y, c2x, c2y, p2.x, p2.y)
    }
    return path
}

/**
 * Calculates perpendicular distance from point p to line segment (a, b).
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
 * Smart Path Tracing Validation (Mode 1: Tracing Line)
 */
fun validateSmartPathTracing(
    userPoints: List<Offset>,
    coverageRatio: Float,
    canvasWidth: Float,
    canvasHeight: Float
): TracingValidationResult {
    if (userPoints.size < 12 || canvasWidth <= 0f || canvasHeight <= 0f) {
        return TracingValidationResult(
            isCorrect = false,
            message = "अक्षर बहुत कम लिखा है! उंगली से चमकती लाइन भरें।"
        )
    }

    val percent = (coverageRatio * 100).toInt()
    if (coverageRatio < 0.80f) {
        return TracingValidationResult(
            isCorrect = false,
            message = "अक्षर अभी $percent% भरा है! कम से कम 80% भरने पर शाबाश मिलेगा!"
        )
    }

    return TracingValidationResult(
        isCorrect = true,
        message = "शाबाश! बहुत अच्छा लिखा!"
    )
}

/**
 * Connect Dots Validation (Mode 2: Connect Dots)
 */
fun validateConnectDotsTracing(
    userPoints: List<Offset>,
    hitDotCount: Int,
    totalDots: Int,
    canvasWidth: Float,
    canvasHeight: Float
): TracingValidationResult {
    if (userPoints.size < 8 || canvasWidth <= 0f || canvasHeight <= 0f) {
        return TracingValidationResult(
            isCorrect = false,
            message = "डॉट्स बहुत कम जोड़े हैं! 1 से शुरू करके सभी डॉट्स मिलाएं।"
        )
    }

    val required = (totalDots * 0.75f).toInt().coerceAtLeast(minOf(2, totalDots))
    if (hitDotCount < required) {
        return TracingValidationResult(
            isCorrect = false,
            message = "अभी कुछ डॉट्स छूटे हैं! सभी $totalDots डॉट्स को मिलाएं।"
        )
    }

    return TracingValidationResult(
        isCorrect = true,
        message = "शाबाश! सारे डॉट्स मिला दिए!"
    )
}
