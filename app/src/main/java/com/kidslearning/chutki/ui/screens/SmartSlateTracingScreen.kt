package com.kidslearning.chutki.ui.screens

import android.graphics.Paint
import androidx.compose.animation.*
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
import com.kidslearning.chutki.model.SubjectType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class AlphabetType { CAPITAL, SMALL, HINDI, NUMBERS }

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
    // Initial category based on subject if provided
    val initialType = when (subject) {
        SubjectType.HINDI -> AlphabetType.HINDI
        SubjectType.ENGLISH -> AlphabetType.CAPITAL
        SubjectType.MATH -> AlphabetType.NUMBERS
        else -> AlphabetType.CAPITAL
    }

    var selectedType by remember { mutableStateOf(initialType) }

    // Pre-defined letter sequences
    val capitalList = remember { ('A'..'Z').map { it.toString() } }
    val smallList = remember { ('a'..'z').map { it.toString() } }
    val hindiList = remember {
        listOf(
            "अ", "आ", "इ", "ई", "उ", "ऊ", "ऋ", "ए", "ऐ", "ओ", "औ", "अं", "अः",
            "क", "ख", "ग", "घ", "ङ", "च", "छ", "ज", "झ", "ञ", "ट", "ठ", "ड", "ढ", "ण",
            "त", "थ", "द", "ध", "न", "प", "फ", "ब", "भ", "म", "य", "र", "ल", "व", "श", "ष", "स", "ह", "क्ष", "त्र", "ज्ञ"
        )
    }
    val numbersList = remember { (1..100).map { it.toString() } }

    val activeLetters = when (selectedType) {
        AlphabetType.CAPITAL -> capitalList
        AlphabetType.SMALL -> smallList
        AlphabetType.HINDI -> hindiList
        AlphabetType.NUMBERS -> numbersList
    }

    var letterIndex by remember(selectedType) {
        // If currentLetter is in the active list, start there; otherwise start at 0
        val idx = activeLetters.indexOf(currentLetter)
        mutableIntStateOf(if (idx >= 0) idx else 0)
    }

    val activeChar = activeLetters.getOrElse(letterIndex) { currentLetter }

    // Dynamic hint for active character
    val activeHint = remember(activeChar, letterHint) {
        getSmartLetterHint(activeChar, letterHint)
    }

    val userStrokes = remember { mutableStateListOf<TracingStroke>() }
    var currentPoints = remember { mutableStateListOf<Offset>() }
    var selectedColor by remember { mutableStateOf(Color(0xFFFFEB3B)) } // Peela magic chalk

    // Feedback states
    var feedbackMessage by remember { mutableStateOf("उंगली से डॉट्स को मिलाते हुए अक्षर बनाओ!") }
    var feedbackStatus by remember { mutableStateOf<Boolean?>(null) } // null: idle, true: correct, false: wrong
    var showDemoGuide by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Announce letter on change
    LaunchedEffect(activeChar) {
        userStrokes.clear()
        currentPoints.clear()
        feedbackStatus = null
        feedbackMessage = "उंगली से डॉट्स को मिलाते हुए अक्षर बनाओ!"
        ttsManager?.speak("स्लेट पर लिखो '$activeChar'")
    }

    // Color Palette
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
                Text(
                    text = "लेवल 2: जादुई स्लेट ✍️",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "अक्षर '$activeChar' लिखो (${letterIndex + 1}/${activeLetters.size})",
                    color = Color(0xFFFFB300),
                    fontSize = 14.sp
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

        // --- 2. Category Tabs ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf(
                AlphabetType.CAPITAL to "A-Z",
                AlphabetType.SMALL to "a-z",
                AlphabetType.HINDI to "अ-ज्ञ",
                AlphabetType.NUMBERS to "1-100"
            ).forEach { (type, title) ->
                val isSelected = selectedType == type
                Button(
                    onClick = {
                        selectedType = type
                        userStrokes.clear()
                        currentPoints.clear()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) Color(0xFF673AB7) else Color(0xFF2C3246)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(title, fontSize = 12.sp, color = Color.White, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
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
                            ttsManager?.speak("ऊपर से शुरू करके नीचे लाएं और डॉट्स मिलाएं!")
                        }
                    }) {
                        Text(
                            text = if (showDemoGuide) "🪄 गाइड छिपाएं" else "🪄 जादू देखो (कैसे लिखें?)",
                            color = Color(0xFF80D8FF),
                            fontSize = 13.sp
                        )
                    }

                    // Status Indicator
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
                    .pointerInput(Unit) {
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
                val canvasWidth = size.width
                val canvasHeight = size.height

                // Draw Slate Guide Lines
                drawLine(
                    color = Color(0x33FFFFFF),
                    start = Offset(40f, canvasHeight * 0.25f),
                    end = Offset(canvasWidth - 40f, canvasHeight * 0.25f),
                    strokeWidth = 2f
                )
                drawLine(
                    color = Color(0x33FFFFFF),
                    start = Offset(40f, canvasHeight * 0.50f),
                    end = Offset(canvasWidth - 40f, canvasHeight * 0.50f),
                    strokeWidth = 2f
                )
                drawLine(
                    color = Color(0x33FFFFFF),
                    start = Offset(40f, canvasHeight * 0.75f),
                    end = Offset(canvasWidth - 40f, canvasHeight * 0.75f),
                    strokeWidth = 2f
                )

                // Dotted Template Character (Native Canvas Text to Path)
                val fontSize = if (activeChar.length > 2) canvasHeight * 0.36f else if (activeChar.length > 1) canvasHeight * 0.44f else canvasHeight * 0.55f
                val textPaint = Paint().apply {
                    textSize = fontSize
                    color = android.graphics.Color.argb(90, 255, 255, 255)
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                    style = Paint.Style.STROKE
                    strokeWidth = 14f
                    pathEffect = android.graphics.DashPathEffect(floatArrayOf(25f, 20f), 0f)
                }

                drawContext.canvas.nativeCanvas.drawText(
                    activeChar,
                    canvasWidth / 2,
                    canvasHeight * 0.68f,
                    textPaint
                )

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
            }

            // Animated Hand Hint (When wrong or requested)
            if (showDemoGuide) {
                Text(
                    text = "👉 ऊपर से शुरू करके नीचे लाएं",
                    color = Color(0xFFFFEB3B),
                    fontSize = 14.sp,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                        .background(Color(0xCC000000), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- 5. Chalk Color Palette & Check Button ---
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

            // Auto-Check Button
            Button(
                onClick = {
                    val allPoints = userStrokes.flatMap { it.points } + currentPoints
                    val isAccurate = validateTracingAccuracy(allPoints, activeChar)

                    if (isAccurate) {
                        feedbackStatus = true
                        feedbackMessage = "बहुत खूब! सही लिखा!"
                        showDemoGuide = false
                        SoundEffectHelper.playCorrectChime()
                        ttsManager?.speak("शाबाश! बहुत सुंदर लिखा!")
                        onLetterTraced?.invoke(activeChar)
                    } else {
                        feedbackStatus = false
                        feedbackMessage = "अक्षर पूरा नहीं हुआ! 'जादू देखो' दबाकर सीखें।"
                        showDemoGuide = true
                        SoundEffectHelper.playSoftTryAgain()
                        ttsManager?.speak("अक्षर पूरा नहीं हुआ! जादू देखो दबाकर सीखें।")
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
                Text("पिछला", color = Color.White)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = {
                    if (letterIndex < activeLetters.size - 1) {
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

/**
 * Smart Stroke Validation Logic:
 * Checks whether user has drawn enough points and covered both height and width boundaries.
 */
fun validateTracingAccuracy(points: List<Offset>, letter: String = ""): Boolean {
    if (points.size < 25) return false // Too short, barely touched

    val minX = points.minOfOrNull { it.x } ?: return false
    val maxX = points.maxOfOrNull { it.x } ?: return false
    val minY = points.minOfOrNull { it.y } ?: return false
    val maxY = points.maxOfOrNull { it.y } ?: return false

    val strokeWidth = maxX - minX
    val strokeHeight = maxY - minY

    val isNarrow = letter in listOf("1", "I", "l", "i", "|", "!", "।")
    val minRequiredWidth = if (isNarrow) 50f else 130f
    val minRequiredHeight = 150f

    // Ensure the child has covered at least a minimal area of the slate
    return strokeWidth > minRequiredWidth && strokeHeight > minRequiredHeight
}

/**
 * Helper to get child-friendly tracing guidance hints.
 */
fun getSmartLetterHint(letter: String, defaultHint: String): String {
    return when (letter.uppercase()) {
        "A" -> "ऊपर से दो तिरछी लाइन (/ \\) खींचें और बीच में सीधी लाइन से जोड़ें।"
        "B" -> "सीधी लाइन खींचें और दाईं ओर दो गोल घुमाव (3) बनाएं।"
        "C" -> "ऊपर से शुरू करके बाईं ओर आधा गोल बनाएं।"
        "D" -> "एक सीधी रेखा खींचें और दाईं ओर बड़ा आधा गोल बनाएं।"
        "E" -> "सीधी खड़ी रेखा खींचें और दाईं ओर तीन छोटी रेखाएं बनाएं।"
        "F" -> "सीधी खड़ी रेखा खींचें और ऊपर दो छोटी रेखाएं बनाएं।"
        "G" -> "गोल घुमाव बनाकर अंदर की तरफ छोटी लाइन खींचें।"
        "H" -> "दो सीधी खड़ी रेखाएं खींचें और बीच में जोड़ें।"
        "I" -> "ऊपर से नीचे एक सीधी खड़ी रेखा खींचें।"
        "J" -> "नीचे लाकर बाईं ओर छोटा हुक घुमाव बनाएं।"
        "K" -> "सीधी लाइन खींचें और दो तिरछी रेखाएं जोड़ें।"
        "L" -> "सीधी खड़ी रेखा खींचें और नीचे दाईं ओर लाइन बनाएं।"
        "M" -> "दो खड़ी रेखाएं खींचें और बीच में V बनाएं।"
        "N" -> "दो खड़ी रेखाएं खींचें और तिरछी लाइन से जोड़ें।"
        "O" -> "पूरा गोल अंडाकार पहिया बनाएं।"
        "P" -> "सीधी रेखा खींचें और ऊपर आधा गोल बनाएं।"
        "Q" -> "गोल बनाकर नीचे दाईं तरफ छोटी पूंछ निकालें।"
        "R" -> "P बनाएं और नीचे एक तिरछी टांग निकालें।"
        "S" -> "सांप की तरह ऊपर से नीचे लहराता हुआ घुमाव बनाएं।"
        "T" -> "ऊपर एक लेटी हुई लाइन और बीच से नीचे सीधी लाइन खींचें।"
        "U" -> "नीचे घुमाकर वापस ऊपर लाएं।"
        "V" -> "दो तिरछी रेखाओं को नीचे एक बिंदु पर मिलाएं।"
        "W" -> "दो V मिलाकर W बनाएं।"
        "X" -> "दो तिरछी रेखाओं को बीच में काटते हुए बनाएं।"
        "Y" -> "ऊपर V बनाएं और नीचे सीधी डंडी जोड़ें।"
        "Z" -> "ऊपर लाइन, तिरछी रेखा और नीचे लेटी हुई लाइन बनाएं।"
        "अ" -> "दो गोल घुमाव (3), बीच में रेखा और दाईं ओर खड़ी रेखा बनाएं।"
        "आ" -> "अ बनाएं और दाईं ओर एक और खड़ी रेखा (आ की मात्रा) जोड़ें।"
        "इ" -> "छोटी खड़ी रेखा खींचकर S की तरह घुमाएं और नीचे गांठ लगाएं।"
        "ई" -> "इ बनाकर ऊपर की ओर सुंदर चंद्र घुमाव बनाएं।"
        "उ" -> "तीन (3) की तरह दो गोल घुमाव बनाएं।"
        "ऊ" -> "उ बनाकर बीच से नीचे पूंछ निकालें।"
        "क" -> "खड़ी रेखा खींचें, बाईं ओर गोल और दाईं ओर खुला घुमाव बनाएं।"
        "ख" -> "ऊपर आधा गोल बनाकर नीचे लाएं और व जोड़ें।"
        "ग" -> "आधा घुमावदार डंडा और पास में बड़ा सीधा डंडा खींचें।"
        "घ" -> "दो उल्टे घुमाव बनाकर सीधी खड़ी रेखा से जोड़ें।"
        "1" -> "ऊपर से नीचे सीधी रेखा खींचें।"
        "2" -> "ऊपर आधा गोल घुमाएं और नीचे लेटी रेखा बनाएं।"
        "3" -> "दो गोल घुमाव एक के नीचे एक बनाएं।"
        "4" -> "तिरछी रेखा, लेटी रेखा और सीधी खड़ी रेखा जोड़ें।"
        "5" -> "लेटी रेखा, छोटी खड़ी रेखा और नीचे गोल पेट बनाएं।"
        else -> defaultHint.ifBlank { "उंगली से डॉट्स को मिलाते हुए अक्षर '$letter' बनाओ!" }
    }
}

