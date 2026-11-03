package com.kidslearning.chutki.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import com.kidslearning.chutki.data.ColoringOutline
import com.kidslearning.chutki.data.LearningContentRepository
import com.kidslearning.chutki.data.ShapeInfo
import com.kidslearning.chutki.data.UserPreferencesManager
import com.kidslearning.chutki.model.SubjectType
import com.kidslearning.chutki.model.UserProfile
import com.kidslearning.chutki.theme.CandyOrange
import com.kidslearning.chutki.theme.CorrectGreen
import com.kidslearning.chutki.ui.components.ClayButton
import com.kidslearning.chutki.ui.components.ClayCard
import com.kidslearning.chutki.ui.components.KidAvatarView
import com.kidslearning.chutki.ui.components.StarConfettiOverlay
import com.kidslearning.chutki.ui.components.TeacherAvatar3D
import com.kidslearning.chutki.ui.components.TeacherPose

enum class Level7Tab {
    SHAPES_MATCH,
    COLOR_PAD
}

@Composable
fun Level7ColorsShapesScreen(
    subject: SubjectType,
    userProfile: UserProfile,
    userPrefs: UserPreferencesManager? = null,
    ttsManager: HindiTtsManager? = null,
    onBack: () -> Unit = {},
    onNavigateToLevel8: () -> Unit = {}
) {
    val isDarkMode = userProfile.isDarkMode
    val isEnglish = userProfile.isEnglish
    var activeTab by remember { mutableStateOf(Level7Tab.SHAPES_MATCH) }

    // --- Shapes Match State ---
    val shapes = remember { LearningContentRepository.getShapesAndColors() }
    var selectedShapeIndex by remember { mutableIntStateOf(0) }
    val currentShape = shapes[selectedShapeIndex]
    val matchedObjects = remember { mutableStateListOf<String>() }

    // --- Color Pad State ---
    val outlines = remember(subject.id) { LearningContentRepository.getColoringOutlines(subject.id) }
    var selectedOutlineIndex by remember { mutableIntStateOf(0) }
    val currentOutline = outlines[selectedOutlineIndex]

    val colorPalette = listOf(
        Color(0xFFFF5252), // Red
        Color(0xFFFF9100), // Orange
        Color(0xFFFFEB3B), // Yellow
        Color(0xFF00E676), // Green
        Color(0xFF00E5FF), // Cyan
        Color(0xFF448AFF), // Blue
        Color(0xFF7C4DFF), // Purple
        Color(0xFFFF4081)  // Pink
    )
    var selectedBrushColor by remember { mutableStateOf(colorPalette[0]) }
    val paintStrokes = remember { mutableStateListOf<TracingStroke>() }
    var currentPaintPoints = remember { mutableStateListOf<Offset>() }
    var outlineFillColor by remember { mutableStateOf<Color?>(null) }

    var showConfetti by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "shape_anim")
    val shapePulse by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shape_pulse"
    )

    // Save auto-resume program
    LaunchedEffect(activeTab) {
        userPrefs?.recordLastActiveProgram(
            type = "LEVEL_7_COLORS_SHAPES",
            subjectId = subject.id,
            levelNumber = 7,
            itemLabel = if (activeTab == Level7Tab.SHAPES_MATCH) "आकार खोज ⭕" else "रंग भरो पैड 🎨"
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (isDarkMode) Brush.verticalGradient(listOf(Color(0xFF0F1226), Color(0xFF191D3C)))
                else Brush.verticalGradient(listOf(Color(0xFFFFF9EE), Color(0xFFFFF0D0)))
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- 1. Top Bar ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = {
                        ttsManager?.stop()
                        onBack()
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isDarkMode) Color(0xFF262A56) else Color(0xFFFFECB3))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = if (isDarkMode) Color.White else Color(0xFF37474F)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isEnglish) "Level 7: Colors & Shapes Explorer 🎨" else "लेवल 7: रंग और आकार खोज 🎨",
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        color = if (isDarkMode) Color.White else Color(0xFF263238)
                    )
                    Text(
                        text = if (activeTab == Level7Tab.SHAPES_MATCH) {
                            if (isEnglish) "Mode: Match Real Objects 🔍" else "मोड: वास्तविक वस्तु मिलान 🔍"
                        } else {
                            if (isEnglish) "Mode: Magic Paint Pad 🖌️" else "मोड: जादुई कलर पैड 🖌️"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CandyOrange
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(CandyOrange.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    KidAvatarView(avatarKey = userProfile.avatarEmoji, size = 36.dp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // --- 2. Top Mode Switcher Tabs ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (activeTab == Level7Tab.SHAPES_MATCH) CandyOrange
                            else if (isDarkMode) Color(0xFF262A56)
                            else Color(0xFFECEFF1)
                        )
                        .clickable {
                            activeTab = Level7Tab.SHAPES_MATCH
                            SoundEffectHelper.playPopSound()
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isEnglish) "🔍 Match Shapes" else "🔍 आकार पहचानो",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = if (activeTab == Level7Tab.SHAPES_MATCH) Color.White else if (isDarkMode) Color.LightGray else Color(0xFF37474F)
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (activeTab == Level7Tab.COLOR_PAD) Color(0xFF673AB7)
                            else if (isDarkMode) Color(0xFF262A56)
                            else Color(0xFFECEFF1)
                        )
                        .clickable {
                            activeTab = Level7Tab.COLOR_PAD
                            SoundEffectHelper.playPopSound()
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isEnglish) "🎨 Paint Pad" else "🎨 रंग भरो पैड",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = if (activeTab == Level7Tab.COLOR_PAD) Color.White else if (isDarkMode) Color.LightGray else Color(0xFF37474F)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // --- 3. TAB 1: SHAPES & REAL OBJECTS MATCHER ---
            if (activeTab == Level7Tab.SHAPES_MATCH) {
                // Shape Selector Chips
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    itemsIndexed(shapes) { idx, shape ->
                        val isSelected = idx == selectedShapeIndex
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                    if (isSelected) CandyOrange
                                    else if (isDarkMode) Color(0xFF262A56)
                                    else Color(0xFFECEFF1)
                                )
                                .clickable {
                                    selectedShapeIndex = idx
                                    SoundEffectHelper.playPopSound()
                                    ttsManager?.speak(
                                        if (isEnglish) "This is a ${shape.nameEnglish}! Tap all objects that have this shape!"
                                        else "यह है ${shape.nameHindi}! उन सभी चीजों को छुओ जो इस आकार की हैं!"
                                    )
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = shape.shapeEmoji, fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isEnglish) shape.nameEnglish else shape.nameHindi,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                    color = if (isSelected) Color.White else if (isDarkMode) Color.LightGray else Color(0xFF37474F)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Current Shape Big Focus Banner
                ClayCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp,
                    bgGradient = Brush.verticalGradient(
                        if (isDarkMode) listOf(Color(0xFF1E2142), Color(0xFF262A56))
                        else listOf(Color.White, Color(0xFFFFF9E6))
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isEnglish) "Find and Tap All Objects with This Shape:" else "इस आकार की सभी वस्तुओं को पहचान कर छुओ:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDarkMode) Color(0xFFA5B0E0) else Color(0xFF546E7A)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .scale(shapePulse)
                                .clip(CircleShape)
                                .background(CandyOrange.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = currentShape.shapeEmoji, fontSize = 52.sp)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (isEnglish) currentShape.nameEnglish else currentShape.nameHindi,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = CandyOrange
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 4 Real Objects Grid (2x2)
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    for (row in 0 until 2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            for (col in 0 until 2) {
                                val objIdx = row * 2 + col
                                if (objIdx < currentShape.realObjects.size) {
                                    val obj = currentShape.realObjects[objIdx]
                                    val isMatched = matchedObjects.contains(obj.nameHindi)

                                    ClayCard(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                if (!isMatched) {
                                                    matchedObjects.add(obj.nameHindi)
                                                    SoundEffectHelper.playCoinSound()
                                                    userPrefs?.recordShapeActivity(subject.id, obj.nameHindi)
                                                    ttsManager?.speak(
                                                        if (isEnglish) "Great! ${obj.nameEnglish} is shaped like a ${currentShape.nameEnglish}!"
                                                        else "शाबाश! ${obj.nameHindi} बिल्कुल ${currentShape.nameHindi} के आकार का होता है!"
                                                    )
                                                    if (matchedObjects.size >= currentShape.realObjects.size) {
                                                        showConfetti = true
                                                        SoundEffectHelper.playStarTwinkle()
                                                    }
                                                }
                                            },
                                        cornerRadius = 20.dp,
                                        bgGradient = Brush.verticalGradient(
                                            if (isMatched) listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9))
                                            else if (isDarkMode) listOf(Color(0xFF262A56), Color(0xFF1E2142))
                                            else listOf(Color.White, Color(0xFFF1F5F9))
                                        ),
                                        borderHighlightColor = if (isMatched) CorrectGreen else Color.Transparent
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(14.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(text = obj.emoji, fontSize = 42.sp)
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = if (isEnglish) obj.nameEnglish else obj.nameHindi,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isMatched) Color(0xFF1B5E20) else if (isDarkMode) Color.White else Color(0xFF263238)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = obj.description,
                                                fontSize = 11.sp,
                                                color = if (isMatched) Color(0xFF2E7D32) else Color.Gray,
                                                textAlign = TextAlign.Center
                                            )

                                            if (isMatched) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(text = "✅ पहचाना!", fontSize = 11.sp, fontWeight = FontWeight.Black, color = CorrectGreen)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- 4. TAB 2: COLOR FILL & PAINT PAD ---
            if (activeTab == Level7Tab.COLOR_PAD) {
                // Outline selector
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    itemsIndexed(outlines) { idx, outline ->
                        val isSelected = idx == selectedOutlineIndex
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                    if (isSelected) Color(0xFF673AB7)
                                    else if (isDarkMode) Color(0xFF262A56)
                                    else Color(0xFFECEFF1)
                                )
                                .clickable {
                                    selectedOutlineIndex = idx
                                    paintStrokes.clear()
                                    currentPaintPoints.clear()
                                    outlineFillColor = null
                                    SoundEffectHelper.playPopSound()
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "${outline.characterOrSymbol} (${if (isEnglish) outline.titleEnglish else outline.titleHindi})",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                color = if (isSelected) Color.White else if (isDarkMode) Color.LightGray else Color(0xFF37474F)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Magic Coloring Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(if (isDarkMode) Color(0xFF131722) else Color(0xFFFBFBFB))
                        .border(3.dp, Color(0xFF673AB7), RoundedCornerShape(22.dp))
                ) {
                    // Big Outline Character in Center (Clickable to fill background)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                outlineFillColor = selectedBrushColor
                                SoundEffectHelper.playCoinSound()
                                userPrefs?.recordShapeActivity(subject.id, currentOutline.id)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentOutline.characterOrSymbol,
                            fontSize = 140.sp,
                            fontWeight = FontWeight.Black,
                            color = outlineFillColor ?: (if (isDarkMode) Color(0x33FFFFFF) else Color(0x22000000))
                        )
                    }

                    // Freehand Paint Strokes Canvas overlay
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        currentPaintPoints.clear()
                                        currentPaintPoints.add(offset)
                                    },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        currentPaintPoints.add(change.position)
                                    },
                                    onDragEnd = {
                                        if (currentPaintPoints.isNotEmpty()) {
                                            paintStrokes.add(
                                                TracingStroke(
                                                    points = currentPaintPoints.toList(),
                                                    color = selectedBrushColor,
                                                    strokeWidth = 34f
                                                )
                                            )
                                            currentPaintPoints.clear()
                                        }
                                    }
                                )
                            }
                    ) {
                        paintStrokes.forEach { stroke ->
                            if (stroke.points.size > 1) {
                                val path = Path().apply {
                                    moveTo(stroke.points.first().x, stroke.points.first().y)
                                    for (i in 1 until stroke.points.size) {
                                        lineTo(stroke.points[i].x, stroke.points[i].y)
                                    }
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
                        }

                        if (currentPaintPoints.size > 1) {
                            val path = Path().apply {
                                moveTo(currentPaintPoints.first().x, currentPaintPoints.first().y)
                                for (i in 1 until currentPaintPoints.size) {
                                    lineTo(currentPaintPoints[i].x, currentPaintPoints[i].y)
                                }
                            }
                            drawPath(
                                path = path,
                                color = selectedBrushColor,
                                style = Stroke(
                                    width = 34f,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }

                    // Helper label at bottom of canvas
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = if (isEnglish) "Tap letter to fill or draw with fingers 🖌️" else "अक्षर पर टैप करके रंग भरो या उंगली से पेंट करो 🖌️",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) Color.Gray else Color(0xFF78909C)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Color Palette & Clear Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        colorPalette.forEach { color ->
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (selectedBrushColor == color) 3.5.dp else 1.dp,
                                        color = if (selectedBrushColor == color) Color.White else Color.Gray,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        selectedBrushColor = color
                                        SoundEffectHelper.playPopSound()
                                    }
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            paintStrokes.clear()
                            currentPaintPoints.clear()
                            outlineFillColor = null
                            SoundEffectHelper.playPopSound()
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF5252))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Clear",
                            tint = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Teacher Priya Didi Encouragement Card
            ClayCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp,
                bgGradient = Brush.horizontalGradient(
                    if (isDarkMode) listOf(Color(0xFF262A56), Color(0xFF1E2142))
                    else listOf(Color(0xFFFFECB3), Color(0xFFFFD54F))
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
                        pose = TeacherPose.POINTING_AT_CARD,
                        isSpeaking = false
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = if (isEnglish) "Teacher Priya says:" else "प्रिया दीदी कहती हैं:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CandyOrange
                        )
                        Text(
                            text = if (isEnglish) "“Look around in your room! Can you find circles, squares and triangles?”"
                            else "“अपने चारों तरफ देखो! रोटी गोल है, समोसा त्रिकोण और खिड़की चौकोर!”",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDarkMode) Color.White else Color(0xFF3E2723)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Next Level 8 Button
            ClayButton(
                onClick = onNavigateToLevel8,
                modifier = Modifier.fillMaxWidth(),
                primaryColor = CorrectGreen
            ) {
                Text(
                    text = if (isEnglish) "Go to Level 8: General Knowledge 🌍" else "लेवल 8: दुनिया की पहचान पर चलें 🌍 ➡️",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }

        StarConfettiOverlay(
            trigger = showConfetti,
            onFinished = { showConfetti = false }
        )
    }
}

