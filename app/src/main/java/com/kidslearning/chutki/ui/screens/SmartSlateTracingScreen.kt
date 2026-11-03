package com.kidslearning.chutki.ui.screens

import android.graphics.DashPathEffect
import android.graphics.Paint
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
import com.kidslearning.chutki.model.SubjectType

enum class AlphabetType { CAPITAL, SMALL, HINDI, MATH }

data class TracingStroke(
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float = 36f
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
    // 1. Determine active subject category
    var selectedType by remember {
        mutableStateOf(
            when {
                subject == SubjectType.MATH || currentLetter.all { it in '0'..'9' } -> AlphabetType.MATH
                subject == SubjectType.HINDI -> AlphabetType.HINDI
                currentLetter.firstOrNull()?.isLowerCase() == true -> AlphabetType.SMALL
                else -> AlphabetType.CAPITAL
            }
        )
    }

    // 2. Letters per category
    val currentLetterList = remember(selectedType) {
        when (selectedType) {
            AlphabetType.CAPITAL -> ('A'..'Z').map { it.toString() }
            AlphabetType.SMALL -> ('a'..'z').map { it.toString() }
            AlphabetType.HINDI -> listOf(
                "अ", "आ", "इ", "ई", "उ", "ऊ", "ऋ", "ए", "ऐ", "ओ", "औ", "अं", "अः",
                "क", "ख", "ग", "घ", "ङ", "च", "छ", "ज", "झ", "ञ",
                "ट", "ठ", "ड", "ढ", "ण", "त", "थ", "द", "ध", "न",
                "प", "फ", "ब", "भ", "म", "य", "र", "ल", "व", "श",
                "ष", "स", "ह", "क्ष", "त्र", "ज्ञ"
            )
            AlphabetType.MATH -> (1..100).map { it.toString() }
        }
    }

    var letterIndex by remember(selectedType) {
        val initialIdx = currentLetterList.indexOf(currentLetter)
        mutableIntStateOf(if (initialIdx >= 0) initialIdx else 0)
    }

    val activeChar = currentLetterList.getOrElse(letterIndex) { currentLetter }

    // Fetch dynamic hint from repository if available
    val activeHint = remember(activeChar, selectedType) {
        when (selectedType) {
            AlphabetType.HINDI -> LearningContentRepository.getAlphabetCards("hindi").find { it.char == activeChar }?.hindiAudioSentence
                ?: "अक्षर '$activeChar' को सुंदर रूप से लिखें।"
            AlphabetType.CAPITAL, AlphabetType.SMALL -> LearningContentRepository.getAlphabetCards("english").find { it.char.equals(activeChar, ignoreCase = true) }?.hindiAudioSentence
                ?: letterHint
            AlphabetType.MATH -> "संख्या '$activeChar' को स्लेट पर लिखें।"
            else -> letterHint
        }
    }

    // Drawing state (Remembered state list for buttery smooth, zero-lag drawing)
    val userStrokes = remember { mutableStateListOf<TracingStroke>() }
    val currentPoints = remember { mutableStateListOf<Offset>() }
    var selectedColor by remember { mutableStateOf(Color(0xFFFFEB3B)) }

    var feedbackStatus by remember { mutableStateOf<Boolean?>(null) }
    var showDemoGuide by remember { mutableStateOf(false) }

    // Audio announcement on letter change
    LaunchedEffect(activeChar) {
        userStrokes.clear()
        currentPoints.clear()
        feedbackStatus = null
        val prompt = when (selectedType) {
            AlphabetType.HINDI -> "जादुई स्लेट पर लिखो अक्षर '$activeChar'"
            AlphabetType.MATH -> "जादुई स्लेट पर लिखो संख्या '$activeChar'"
            else -> "Write letter '$activeChar' on the slate"
        }
        ttsManager?.speak(prompt)
    }

    val colors = listOf(
        Color(0xFFFFEB3B), // Yellow
        Color(0xFF00E676), // Green
        Color(0xFF00E5FF), // Cyan
        Color(0xFFFF4081), // Pink
        Color(0xFFFF9100), // Orange
        Color(0xFFFFFFFF)  // White
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF131722))
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
                Text(
                    text = "लेवल 2: जादुई स्लेट ✍️",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${if (selectedType == AlphabetType.MATH) "संख्या" else "अक्षर"} '$activeChar' (${letterIndex + 1}/${currentLetterList.size})",
                    color = Color(0xFFFFB300),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            IconButton(
                onClick = {
                    SoundEffectHelper.playPopSound()
                    userStrokes.clear()
                    currentPoints.clear()
                    feedbackStatus = null
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFFFF5252))
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Clear", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- 2. Category Tabs ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val categories = if (subject == SubjectType.MATH) {
                listOf(
                    AlphabetType.MATH to "संख्या (1-100)",
                    AlphabetType.HINDI to "वर्णमाला (अ-ज्ञ)",
                    AlphabetType.CAPITAL to "Capital (A-Z)"
                )
            } else {
                listOf(
                    AlphabetType.CAPITAL to "Capital (A-Z)",
                    AlphabetType.SMALL to "Small (a-z)",
                    AlphabetType.HINDI to "वर्णमाला (अ-ज्ञ)"
                )
            }

            categories.forEach { (type, title) ->
                val isSelected = selectedType == type
                Button(
                    onClick = {
                        if (selectedType != type) {
                            selectedType = type
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

        // --- 3. Guidance Box ---
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
                    fontSize = 13.sp,
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
                            ttsManager?.speak("डैश लाइन के ऊपर-ऊपर उंगली चलाएं!")
                        }
                    }) {
                        Text(
                            text = if (showDemoGuide) "🪄 गाइड छिपाएं" else "🪄 जादू देखो (कैसे लिखें?)",
                            color = Color(0xFF80D8FF),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (feedbackStatus != null) {
                        Text(
                            text = if (feedbackStatus == true) "✅ शाबाश! बहुत अच्छा" else "❌ फिर से कोशिश करो!",
                            color = if (feedbackStatus == true) Color(0xFF00E676) else Color(0xFFFF5252),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- 4. Black Slate Canvas: Solid Watermark + Dashed Tracing Line (No Extra Dots) ---
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
                                currentPoints.clear()
                                currentPoints.add(offset)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                currentPoints.add(change.position)
                            },
                            onDragEnd = {
                                if (currentPoints.isNotEmpty()) {
                                    userStrokes.add(
                                        TracingStroke(
                                            points = currentPoints.toList(),
                                            color = selectedColor,
                                            strokeWidth = 36f
                                        )
                                    )
                                    currentPoints.clear()
                                }
                            },
                            onDragCancel = {
                                currentPoints.clear()
                            }
                        )
                    }
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                // Guide Lines
                drawLine(
                    color = Color(0x22FFFFFF),
                    start = Offset(40f, canvasHeight * 0.25f),
                    end = Offset(canvasWidth - 40f, canvasHeight * 0.25f),
                    strokeWidth = 2f
                )
                drawLine(
                    color = Color(0x22FFFFFF),
                    start = Offset(40f, canvasHeight * 0.75f),
                    end = Offset(canvasWidth - 40f, canvasHeight * 0.75f),
                    strokeWidth = 2f
                )

                // Font size scaling for single, double, or triple digits
                val fontScale = if (activeChar.length > 2) 0.38f else if (activeChar.length > 1) 0.46f else 0.55f

                // Layer 1: Soft Natural Watermark Letter
                val watermarkPaint = Paint().apply {
                    textSize = canvasHeight * fontScale
                    color = android.graphics.Color.argb(35, 255, 255, 255)
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                    style = Paint.Style.FILL
                }

                drawContext.canvas.nativeCanvas.drawText(
                    activeChar,
                    canvasWidth / 2,
                    canvasHeight * 0.68f,
                    watermarkPaint
                )

                // Layer 2: Clean Dashed Stroke Effect (Bina kisi overlapping dot ke)
                val dashStrokePaint = Paint().apply {
                    textSize = canvasHeight * fontScale
                    color = android.graphics.Color.argb(130, 129, 212, 250) // Soft sky blue dashed trace
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                    style = Paint.Style.STROKE
                    strokeWidth = 8f
                    pathEffect = DashPathEffect(floatArrayOf(24f, 18f), 0f)
                }

                drawContext.canvas.nativeCanvas.drawText(
                    activeChar,
                    canvasWidth / 2,
                    canvasHeight * 0.68f,
                    dashStrokePaint
                )

                // Layer 3: Bachhe ki banayi hui strokes (User Draw)
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

                // Live drawing stroke (Real-time smooth chalk)
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
            }

            if (showDemoGuide) {
                Text(
                    text = "👉 डैश लाइन के ऊपर-ऊपर उंगली चलाएं",
                    color = Color(0xFFFFEB3B),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                        .background(Color(0xCC000000), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- 5. Chalk Colors & Check Button ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                colors.forEach { color ->
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

            Button(
                onClick = {
                    val allPoints = userStrokes.flatMap { it.points } + currentPoints
                    val isAccurate = validateTracingAccuracy(allPoints)

                    if (isAccurate) {
                        feedbackStatus = true
                        showDemoGuide = false
                        SoundEffectHelper.playCorrectChime()
                        ttsManager?.speak("शाबाश! बहुत अच्छा लिखा!")
                        onLetterTraced?.invoke(activeChar)
                    } else {
                        feedbackStatus = false
                        showDemoGuide = true
                        SoundEffectHelper.playSoftTryAgain()
                        ttsManager?.speak("फिर से कोशिश करो! डैश लाइन के ऊपर उंगली चलाएं।")
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
                Text("पिछला", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = {
                    if (letterIndex < currentLetterList.size - 1) {
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
                Text("अगला अक्षर", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White)
            }
        }
    }
}

fun validateTracingAccuracy(points: List<Offset>): Boolean {
    if (points.size < 25) return false

    val minX = points.minOf { it.x }
    val maxX = points.maxOf { it.x }
    val minY = points.minOf { it.y }
    val maxY = points.maxOf { it.y }

    val strokeWidth = maxX - minX
    val strokeHeight = maxY - minY

    return strokeWidth > 140f && strokeHeight > 180f
}
